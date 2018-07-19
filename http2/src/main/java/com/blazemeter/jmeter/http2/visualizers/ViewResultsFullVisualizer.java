package com.blazemeter.jmeter.http2.visualizers;

import com.blazemeter.jmeter.http2.sampler.HTTP2SampleResult;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.apache.commons.collections.EnumerationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.JMeter;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.ResultRenderer;
import org.apache.jmeter.visualizers.SearchTreePanel;
import org.apache.jmeter.visualizers.SearchableTreeNode;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a View Result Tree which shows the samplers with pending response different than the
 * samplers already responded
 */
public class ViewResultsFullVisualizer extends AbstractVisualizer
    implements ActionListener, TreeSelectionListener, Clearable, ItemListener {

  private static final Logger LOG = LoggerFactory
      .getLogger(org.apache.jmeter.visualizers.ViewResultsFullVisualizer.class);
  public static final String VIEW_RESULT_TREE_HTTP2_TITLE = "View Result Tree Http2";
  protected static final String COMBO_CHANGE_COMMAND = "change_combo";
  private static final Border RED_BORDER = BorderFactory.createLineBorder(Color.red);
  private static final Border BLUE_BORDER = BorderFactory.createLineBorder(Color.blue);
  private static final String ICON_SIZE = JMeterUtils
      .getPropDefault(JMeter.TREE_ICON_SIZE, JMeter.DEFAULT_TREE_ICON_SIZE);
  private static final String VIEWERS_ORDER =
      JMeterUtils
          .getPropDefault("view.results.tree.renderers_order", "");
  private static final int REFRESH_PERIOD = JMeterUtils
      .getPropDefault("jmeter.gui.refresh_period", 500);
  private static final ImageIcon imageSuccess = JMeterUtils.getImage(
      JMeterUtils.getPropDefault("viewResultsTree.success",
          "vrt/" + ICON_SIZE + "/security-high-2.png"));
  private static final ImageIcon imagePending = JMeterUtils
      .getImage("toolbar/" + ICON_SIZE + "arrow-right-3.png");
  private static final ImageIcon imageFailure = JMeterUtils.getImage(
      JMeterUtils.getPropDefault("viewResultsTree.failure",
          "vrt/" + ICON_SIZE + "/security-low-2.png"));

  private JSplitPane mainSplit;
  private DefaultMutableTreeNode root;
  private DefaultTreeModel treeModel;
  private JTree jTree;
  private Component leftSide;
  private JTabbedPane rightSide;
  private JComboBox<ResultRenderer> selectRenderPanel;
  private int selectedTab;
  private ResultRenderer resultsRender = null;
  private Object resultsObject = null;
  private TreeSelectionEvent lastSelectionEvent;
  private JCheckBox autoScrollCB;
  private HashSet buffer;
  private boolean dataChanged;

  @Override
  public String getLabelResource() {
    return VIEW_RESULT_TREE_HTTP2_TITLE;
  }

  @Override
  public String getStaticLabel() {
    return VIEW_RESULT_TREE_HTTP2_TITLE;
  }

  public ViewResultsFullVisualizer() {
    collector = new ResultCollector();
    buffer = new LinkedHashSet();
    init();
    new Timer(REFRESH_PERIOD, e -> updateGui()).start();
  }

  private void updateGui() {
    TreePath selectedPath = null;
    Object oldSelectedElement;
    Set<Object> oldExpandedElements;
    Set<TreePath> newExpandedPaths = new HashSet<>();
    synchronized (buffer) {
      if (!dataChanged) {
        return;
      }

      final Enumeration<TreePath> expandedElements = jTree
          .getExpandedDescendants(new TreePath(root));
      oldExpandedElements = extractExpandedObjects(expandedElements);
      oldSelectedElement = getSelectedObject();
      root.removeAllChildren();
      for (Object sampler : buffer) {
        SampleResult res = (SampleResult) sampler;
        // Add sample
        DefaultMutableTreeNode currNode = new SearchableTreeNode(res, treeModel);
        treeModel.insertNodeInto(currNode, root, root.getChildCount());
        List<TreeNode> path = new ArrayList<>(Arrays.asList(root, currNode));
        selectedPath = checkExpandedOrSelected(path,
            res, oldSelectedElement,
            oldExpandedElements, newExpandedPaths, selectedPath);
        TreePath potentialSelection = addSubResults(currNode, res, path, oldSelectedElement,
            oldExpandedElements, newExpandedPaths);
        if (potentialSelection != null) {
          selectedPath = potentialSelection;
        }
        // Add any assertion that failed as children of the sample node
        AssertionResult[] assertionResults = res.getAssertionResults();
        int assertionIndex = currNode.getChildCount();
        for (AssertionResult assertionResult : assertionResults) {
          if (assertionResult.isFailure() || assertionResult.isError()) {
            DefaultMutableTreeNode assertionNode = new SearchableTreeNode(assertionResult,
                treeModel);
            treeModel.insertNodeInto(assertionNode, currNode, assertionIndex++);
            selectedPath = checkExpandedOrSelected(path,
                assertionResult, oldSelectedElement,
                oldExpandedElements, newExpandedPaths, selectedPath,
                assertionNode);
          }
        }
      }
      treeModel.nodeStructureChanged(root);
      dataChanged = false;
    }

    if (root.getChildCount() == 1) {
      jTree.expandPath(new TreePath(root));
    }
    newExpandedPaths.stream().forEach(jTree::expandPath);
    if (selectedPath != null) {
      jTree.setSelectionPath(selectedPath);
    }
    if (autoScrollCB.isSelected() && root.getChildCount() > 1) {
      jTree.scrollPathToVisible(new TreePath(new Object[]{root,
          treeModel.getChild(root, root.getChildCount() - 1)}));
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();
    if (COMBO_CHANGE_COMMAND.equals(command)) {
      JComboBox<?> jcb = (JComboBox<?>) e.getSource();

      if (jcb != null) {
        resultsRender = (ResultRenderer) jcb.getSelectedItem();
        if (rightSide != null) {
          // to restore last selected tab (better user-friendly)
          selectedTab = rightSide.getSelectedIndex();
          // Remove old right side
          mainSplit.remove(rightSide);

          // create and add a new right side
          rightSide = new JTabbedPane();
          mainSplit.add(rightSide);
          resultsRender.setRightSide(rightSide);
          resultsRender.setLastSelectedTab(selectedTab);
          LOG.debug("selectedTab={}", selectedTab);
          resultsRender.init();
          // To display current sampler result before change
          this.valueChanged(lastSelectionEvent, true);
        }
      }
    }
  }

  @Override
  public void itemStateChanged(ItemEvent e) {
    //no action
  }

  @Override
  public void valueChanged(TreeSelectionEvent e) {
    valueChanged(e, false);
  }

  private void valueChanged(TreeSelectionEvent e, boolean forceRendering) {
    lastSelectionEvent = e;
    DefaultMutableTreeNode node;
    synchronized (this) {
      node = (DefaultMutableTreeNode) jTree.getLastSelectedPathComponent();
    }

    if (node != null && (forceRendering || node.getUserObject() != resultsObject)) {
      resultsObject = node.getUserObject();
      // to restore last tab used
      if (rightSide.getTabCount() > selectedTab) {
        resultsRender.setLastSelectedTab(rightSide.getSelectedIndex());
      }
      Object userObject = node.getUserObject();
      resultsRender.setSamplerResult(userObject);
      resultsRender.setupTabPane(); // Processes Assertions
      // display a SampleResult
      if (userObject instanceof SampleResult) {
        SampleResult sampleResult = (SampleResult) userObject;
        if (isTextDataType(sampleResult)) {
          resultsRender.renderResult(sampleResult);
        } else {
          byte[] responseBytes = sampleResult.getResponseData();
          if (responseBytes != null) {
            resultsRender.renderImage(sampleResult);
          }
        }
      }
    }
  }

  private static boolean isTextDataType(SampleResult sampleResult) {
    return (SampleResult.TEXT).equals(sampleResult.getDataType())
        || StringUtils.isEmpty(sampleResult.getDataType());
  }

  @Override
  public void clearData() {
    synchronized (buffer) {
      buffer.clear();
      dataChanged = true;
    }
    resultsRender.clearData();
    resultsObject = null;
  }

  @Override
  public void add(SampleResult sampleResult) {
    synchronized (buffer) {
      buffer.add(sampleResult);
      dataChanged = true;
    }
  }

  /**
   * Initialize this visualizer
   */
  private void init() {  // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
    LOG.debug("init() - pass");
    setLayout(new BorderLayout(0, 5));
    setBorder(makeBorder());

    leftSide = createLeftPanel();
    // Prepare the common tab
    rightSide = new JTabbedPane();

    // Create the split pane
    mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSide, rightSide);
    mainSplit.setOneTouchExpandable(true);

    JSplitPane searchAndMainSP = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
        new SearchTreePanel(root), mainSplit);
    searchAndMainSP.setOneTouchExpandable(true);
    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, makeTitlePanel(),
        searchAndMainSP);
    splitPane.setOneTouchExpandable(true);
    add(splitPane);

    // init right side with first render
    resultsRender.setRightSide(rightSide);
    resultsRender.init();
  }

  private synchronized Component createLeftPanel() {
    SampleResult rootSampleResult = new SampleResult();
    rootSampleResult.setSampleLabel("Root");
    rootSampleResult.setSuccessful(true);
    root = new SearchableTreeNode(rootSampleResult, null);

    treeModel = new DefaultTreeModel(root);
    jTree = new JTree(treeModel);
    jTree.setCellRenderer(new ResultsNodeRenderer());
    jTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    jTree.addTreeSelectionListener(this);
    jTree.setRootVisible(false);
    jTree.setShowsRootHandles(true);
    JScrollPane treePane = new JScrollPane(jTree);
    treePane.setPreferredSize(new Dimension(200, 300));

    VerticalPanel leftPane = new VerticalPanel();
    leftPane.add(treePane, BorderLayout.CENTER);
    leftPane.add(createComboRender(), BorderLayout.NORTH);
    autoScrollCB = new JCheckBox(
        JMeterUtils.getResString("view_results_autoscroll"));
    autoScrollCB.setSelected(false);
    autoScrollCB.addItemListener(this);
    leftPane.add(autoScrollCB, BorderLayout.SOUTH);
    return leftPane;
  }

  private static class ResultsNodeRenderer extends DefaultTreeCellRenderer {

    private static final long serialVersionUID = 4159626601097711565L;

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
        boolean sel, boolean expanded, boolean leaf, int row, boolean focus) {
      super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, focus);
      boolean failure = true;
      boolean pending = false;
      Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
      if (userObject instanceof SampleResult) {
        failure = !(((SampleResult) userObject).isSuccessful());
        if (userObject instanceof HTTP2SampleResult) {
          pending = ((HTTP2SampleResult) userObject).isPendingResponse();
        }
      } else if (userObject instanceof AssertionResult) {
        AssertionResult assertion = (AssertionResult) userObject;
        failure = assertion.isError() || assertion.isFailure();
      }

      // Set the status for the node
      if (failure && !pending) {
        this.setForeground(Color.red);
        this.setIcon(imageFailure);
      } else if (pending) {
        this.setIcon(imagePending);
        this.setForeground(Color.blue);
      } else {
        this.setIcon(imageSuccess);
      }

      // Handle search related rendering
      SearchableTreeNode node = (SearchableTreeNode) value;
      if (node.isNodeHasMatched()) {
        setBorder(RED_BORDER);
      } else if (node.isChildrenNodesHaveMatched()) {
        setBorder(BLUE_BORDER);
      } else {
        setBorder(null);
      }
      return this;
    }
  }

  private Component createComboRender() {
    ComboBoxModel<ResultRenderer> nodesModel = new DefaultComboBoxModel<>();
    // drop-down list for renderer
    selectRenderPanel = new JComboBox<>(nodesModel);
    selectRenderPanel.setActionCommand(COMBO_CHANGE_COMMAND);
    selectRenderPanel.addActionListener(this);

    // if no results render in jmeter.properties, load Standard (default)
    List<String> classesToAdd = Collections.<String>emptyList();
    try {
      classesToAdd = JMeterUtils.findClassesThatExtend(ResultRenderer.class);
    } catch (IOException e1) {
      // ignored
    }
    String textRenderer = JMeterUtils.getResString("view_results_render_text");
    Object textObject = null;
    Map<String, ResultRenderer> map = new HashMap<>(classesToAdd.size());
    for (String clazz : classesToAdd) {
      try {
        // Instantiate render classes
        final ResultRenderer renderer = (ResultRenderer) Class.forName(clazz).newInstance();
        if (textRenderer.equals(renderer.toString())) {
          textObject = renderer;
        }
        renderer.setBackgroundColor(getBackground());
        map.put(renderer.getClass().getName(), renderer);
      } catch (Exception | NoClassDefFoundError e) { // NOSONAR See bug 60583
        LOG.warn("Error loading result renderer: {}", clazz, e);
      }
    }
    if (VIEWERS_ORDER.length() > 0) {
      Arrays.stream(VIEWERS_ORDER.split(","))
          .map(key -> key.startsWith(".")
              ? "org.apache.jmeter.visualizers" + key
              : key)
          .forEach(key -> {
            ResultRenderer renderer = map.remove(key);
            if (renderer != null) {
              selectRenderPanel.addItem(renderer);
            } else {
              LOG.warn(
                  "Missing (check renderer name) or already added (check doublon) result renderer,"
                      +
                      " check property 'view.results.tree.renderers_order', renderer name: '{}'",
                  key);
            }
          });
    }
    // Add remaining (plugins or missed in property)
    map.values().forEach(renderer -> selectRenderPanel.addItem(renderer));
    nodesModel.setSelectedItem(textObject); // preset to "Text" option
    return selectRenderPanel;
  }

  private Set<Object> extractExpandedObjects(final Enumeration<TreePath> expandedElements) {
    if (expandedElements != null) {
      @SuppressWarnings("unchecked") final List<TreePath> list = EnumerationUtils
          .toList(expandedElements);
      LOG.debug("Expanded: {}", list);
      Set<Object> result = list.stream()
          .map(TreePath::getLastPathComponent)
          .map(c -> (DefaultMutableTreeNode) c)
          .map(DefaultMutableTreeNode::getUserObject)
          .collect(Collectors.toSet());
      LOG.debug("Elements: {}", result);
      return result;
    }
    return Collections.emptySet();
  }

  private TreePath addSubResults(DefaultMutableTreeNode currNode,
      SampleResult res, List<TreeNode> path, Object selectedObject,
      Set<Object> oldExpandedObjects, Set<TreePath> newExpandedPaths) {
    SampleResult[] subResults = res.getSubResults();

    int leafIndex = 0;
    TreePath result = null;

    for (SampleResult child : subResults) {
      LOG.debug("updateGui1 : child sample result - {}", child);
      DefaultMutableTreeNode leafNode = new SearchableTreeNode(child, treeModel);

      treeModel.insertNodeInto(leafNode, currNode, leafIndex++);
      List<TreeNode> newPath = new ArrayList<>(path);
      newPath.add(leafNode);
      result = checkExpandedOrSelected(newPath, child, selectedObject, oldExpandedObjects,
          newExpandedPaths, result);
      addSubResults(leafNode, child, newPath, selectedObject, oldExpandedObjects, newExpandedPaths);
      // Add any assertion that failed as children of the sample node
      AssertionResult[] assertionResults = child.getAssertionResults();
      int assertionIndex = leafNode.getChildCount();
      for (AssertionResult item : assertionResults) {
        if (item.isFailure() || item.isError()) {
          DefaultMutableTreeNode assertionNode = new SearchableTreeNode(item, treeModel);
          treeModel.insertNodeInto(assertionNode, leafNode, assertionIndex++);
          result = checkExpandedOrSelected(path, item,
              selectedObject, oldExpandedObjects, newExpandedPaths, result,
              assertionNode);
        }
      }
    }
    return result;
  }

  private Object getSelectedObject() {
    Object oldSelectedElement;
    DefaultMutableTreeNode oldSelectedNode = (DefaultMutableTreeNode) jTree
        .getLastSelectedPathComponent();
    oldSelectedElement = oldSelectedNode == null ? null : oldSelectedNode.getUserObject();
    return oldSelectedElement;
  }

  private TreePath checkExpandedOrSelected(List<TreeNode> path,
      Object item, Object oldSelectedObject,
      Set<Object> oldExpandedObjects, Set<TreePath> newExpandedPaths,
      TreePath defaultPath) {
    TreePath result = defaultPath;
    if (oldSelectedObject == item) {
      result = toTreePath(path);
    }
    if (oldExpandedObjects.contains(item)) {
      newExpandedPaths.add(toTreePath(path));
    }
    return result;
  }

  private TreePath checkExpandedOrSelected(List<TreeNode> path,
      Object item, Object oldSelectedObject,
      Set<Object> oldExpandedObjects, Set<TreePath> newExpandedPaths,
      TreePath defaultPath, DefaultMutableTreeNode extensionNode) {
    TreePath result = defaultPath;
    if (oldSelectedObject == item) {
      result = toTreePath(path, extensionNode);
    }
    if (oldExpandedObjects.contains(item)) {
      newExpandedPaths.add(toTreePath(path, extensionNode));
    }
    return result;
  }

  private TreePath toTreePath(List<TreeNode> newPath) {
    return new TreePath(newPath.toArray(new TreeNode[newPath.size()]));
  }

  private TreePath toTreePath(List<TreeNode> path,
      DefaultMutableTreeNode extensionNode) {
    TreeNode[] result = path.toArray(new TreeNode[path.size() + 1]);
    result[result.length - 1] = extensionNode;
    return new TreePath(result);
  }
}
