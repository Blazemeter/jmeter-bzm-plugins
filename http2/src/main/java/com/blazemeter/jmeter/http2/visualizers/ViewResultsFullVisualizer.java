/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 *
 */
package com.blazemeter.jmeter.http2.visualizers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import javax.swing.border.Border;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.JMeter;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.SearchTreePanel;
import org.apache.jmeter.visualizers.SearchableTreeNode;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.apache.jmeter.visualizers.ResultRenderer;

import com.blazemeter.jmeter.http2.sampler.HTTP2SampleResult;

/**
 * Base for ViewResults
 *
 */
public class ViewResultsFullVisualizer extends AbstractVisualizer
implements ActionListener, TreeSelectionListener, Clearable, ItemListener {

    private static final long serialVersionUID = 7338676747296593842L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    public static final Color SERVER_ERROR_COLOR = Color.red;

    public static final Color CLIENT_ERROR_COLOR = Color.blue;

    public static final Color REDIRECT_COLOR = Color.green;
    
    private static final Border RED_BORDER = BorderFactory.createLineBorder(Color.red);
    
    private static final Border BLUE_BORDER = BorderFactory.createLineBorder(Color.blue);

    private  JSplitPane mainSplit;

    private DefaultMutableTreeNode root;

    private DefaultTreeModel treeModel;

    private JTree jTree;

    private Component leftSide;

    private JTabbedPane rightSide;

    private JComboBox<ResultRenderer> selectRenderPanel;

    private int selectedTab;

    protected static final String COMBO_CHANGE_COMMAND = "change_combo"; // $NON-NLS-1$

    private static final String iconSize = JMeterUtils.getPropDefault(JMeter.TREE_ICON_SIZE, JMeter.DEFAULT_TREE_ICON_SIZE);

    private static final ImageIcon imageSuccess = JMeterUtils.getImage(
            JMeterUtils.getPropDefault("viewResultsTree.success",  //$NON-NLS-1$
                    "vrt/" + iconSize + "/security-high-2.png")); //$NON-NLS-1$ $NON-NLS-2$

    private static final ImageIcon imageFailure = JMeterUtils.getImage(
            JMeterUtils.getPropDefault("viewResultsTree.failure",  //$NON-NLS-1$
                    "vrt/" + iconSize + "/security-low-2.png")); //$NON-NLS-1$ $NON-NLS-2$

    // Maximum size that we will display
    // Default limited to 10 megabytes
    private static final int MAX_DISPLAY_SIZE =
        JMeterUtils.getPropDefault("view.results.tree.max_size", 10485760); // $NON-NLS-1$

    // default display order
    private static final String VIEWERS_ORDER =
        JMeterUtils.getPropDefault("view.results.tree.renderers_order", ""); // $NON-NLS-1$ //$NON-NLS-2$
    
    public static final String VIEW_RESULT_TREE_HTTP2_TITLE="View Result Tree Http2";

    private ResultRenderer resultsRender = null;

    private TreeSelectionEvent lastSelectionEvent;

    private JCheckBox autoScrollCB;

    /**
     * Constructor
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    public ViewResultsFullVisualizer() {
        super();
        init();
    }
    
    public DefaultMutableTreeNode getRoot(){
    	return root;
    }
    
    public DefaultTreeModel getTreeModel() {
    	return treeModel;
    }

    /** {@inheritDoc} */
    @Override
    public void add(final SampleResult sample) {
        JMeterUtils.runSafe(false, new Runnable() {
            @Override
            public void run() {
                updateGui(sample);
            }
        });
    }
    
    public JComboBox<ResultRenderer> getSelectRenderPanel(){
    	return selectRenderPanel;
    }

    /**
     * Update the visualizer with new data.
     */
    private synchronized void updateGui(SampleResult res) {
        // Add sample
        DefaultMutableTreeNode currNode = new SearchableTreeNode(res, treeModel);
        treeModel.insertNodeInto(currNode, root, root.getChildCount());
        addSubResults(currNode, res);
        // Add any assertion that failed as children of the sample node
        AssertionResult[] assertionResults = res.getAssertionResults();
        int assertionIndex = currNode.getChildCount();
        for (AssertionResult assertionResult : assertionResults) {
            if (assertionResult.isFailure() || assertionResult.isError()) {
                DefaultMutableTreeNode assertionNode = new SearchableTreeNode(assertionResult, treeModel);
                treeModel.insertNodeInto(assertionNode, currNode, assertionIndex++);
            }
        }

        if (root.getChildCount() == 1) {
            jTree.expandPath(new TreePath(root));
        }
        if (autoScrollCB.isSelected() && root.getChildCount() > 1) {
            jTree.scrollPathToVisible(new TreePath(new Object[] { root,
                    treeModel.getChild(root, root.getChildCount() - 1) }));
        }
    }

    public void addSubResults(DefaultMutableTreeNode currNode, SampleResult res) {
        SampleResult[] subResults = res.getSubResults();
		 
        int leafIndex = 0;

        for (SampleResult child : subResults) {
          	if (child instanceof HTTP2SampleResult){
        		HTTP2SampleResult http2Result= (HTTP2SampleResult) child;
        		if (http2Result.isSecondaryRequest()){
        			if (log.isDebugEnabled()) {
                        log.debug("updateGui1 : child sample result - " + child);
                    }
                    DefaultMutableTreeNode leafNode = new SearchableTreeNode(child, treeModel);

                    treeModel.insertNodeInto(leafNode, currNode, leafIndex++);
                    addSubResults(leafNode, child);
                    // Add any assertion that failed as children of the sample node
                    AssertionResult[] assertionResults = child.getAssertionResults();
                    int assertionIndex = leafNode.getChildCount();
                    for (AssertionResult item : assertionResults) {
                        if (item.isFailure() || item.isError()) {
                            DefaultMutableTreeNode assertionNode = new SearchableTreeNode(item, treeModel);
                            treeModel.insertNodeInto(assertionNode, leafNode, assertionIndex++);                      
                        }
                    }
	
        		} else {
        			// is a request that with a later response
        			int id=http2Result.getId();
            		DefaultMutableTreeNode node=findNode((DefaultMutableTreeNode) treeModel.getRoot(), id);
            		if (node!=null){
            			// Update the node that corresponds to the http2Result
            			node.setUserObject(http2Result);
            			addSubResults(node, http2Result);
	
            		}        			
        		}
        		
        		
        	} else {
        		if (log.isDebugEnabled()) {
                    log.debug("updateGui1 : child sample result - " + child);
                }
                DefaultMutableTreeNode leafNode = new SearchableTreeNode(child, treeModel);

                treeModel.insertNodeInto(leafNode, currNode, leafIndex++);
                addSubResults(leafNode, child);
                // Add any assertion that failed as children of the sample node
                AssertionResult[] assertionResults = child.getAssertionResults();
                int assertionIndex = leafNode.getChildCount();
                for (AssertionResult item : assertionResults) {
                    if (item.isFailure() || item.isError()) {
                        DefaultMutableTreeNode assertionNode = new SearchableTreeNode(item, treeModel);
                        treeModel.insertNodeInto(assertionNode, leafNode, assertionIndex++);
                    }
                }
        	}
            
        }
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void clearData() {
        while (root.getChildCount() > 0) {
            // the child to be removed will always be 0 'cos as the nodes are
            // removed the nth node will become (n-1)th
            treeModel.removeNodeFromParent((DefaultMutableTreeNode) root.getChildAt(0));
        }
        resultsRender.clearData();
    }

    /** {@inheritDoc} */
    @Override
    public String getLabelResource() {
        return VIEW_RESULT_TREE_HTTP2_TITLE; // $NON-NLS-1$
    }
    
    @Override
    public String getStaticLabel() {
        return VIEW_RESULT_TREE_HTTP2_TITLE;
    }

    /**
     * Initialize this visualizer
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    private void init() {  // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        log.debug("init() - pass");
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);

        leftSide = createLeftPanel();
        // Prepare the common tab
        rightSide = new JTabbedPane();

        // Create the split pane
        mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSide, rightSide);
        mainSplit.setOneTouchExpandable(true);

        JSplitPane searchAndMainSP = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
                new SearchTreePanel(root), mainSplit);
        searchAndMainSP.setOneTouchExpandable(true);
        add(searchAndMainSP, BorderLayout.CENTER);
        // init right side with first render
        resultsRender.setRightSide(rightSide);
        resultsRender.init();
    }

    /** {@inheritDoc} */
    @Override
    public void valueChanged(TreeSelectionEvent e) {
        lastSelectionEvent = e;
        DefaultMutableTreeNode node = null;
        synchronized (this) {
            node = (DefaultMutableTreeNode) jTree.getLastSelectedPathComponent();
        }

        if (node != null) {
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
                if (isTextDataType(sampleResult)){
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

    /**
     * @param sampleResult SampleResult
     * @return true if sampleResult is text or has empty content type
     */
    protected static boolean isTextDataType(SampleResult sampleResult) {
        return (SampleResult.TEXT).equals(sampleResult.getDataType())
                || StringUtils.isEmpty(sampleResult.getDataType());
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
        autoScrollCB = new JCheckBox(JMeterUtils.getResString("view_results_autoscroll")); // $NON-NLS-1$
        autoScrollCB.setSelected(false);
        autoScrollCB.addItemListener(this);
        leftPane.add(autoScrollCB, BorderLayout.SOUTH);
        return leftPane;
    }

    /**
     * Create the drop-down list to changer render
     * @return List of all render (implement ResultsRender)
     */
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
            if (classesToAdd.isEmpty()){
            	classesToAdd.add("org.apache.jmeter.visualizers.RenderAsText");
            }
        } catch (IOException e1) {
            // ignored
        }
        String textRenderer = JMeterUtils.getResString("view_results_render_text"); // $NON-NLS-1$
        Object textObject = null;
        Map<String, ResultRenderer> map = new HashMap<>(classesToAdd.size());
        for (String clazz : classesToAdd) {
            try {
                // Instantiate render classes
                final ResultRenderer renderer = (ResultRenderer) Class.forName(clazz).newInstance();
                if (textRenderer.equals(renderer.toString())){
                    textObject=renderer;
                }
                renderer.setBackgroundColor(getBackground());
                map.put(renderer.getClass().getName(), renderer);
            } catch (Exception e) {
                log.warn("Error loading result renderer:" + clazz, e);
            }
        }
        if(VIEWERS_ORDER.length()>0) {
            String[] keys = VIEWERS_ORDER.split(",");
            for (String key : keys) {
                if(key.startsWith(".")) {
                    key = "org.apache.jmeter.visualizers"+key; //$NON-NLS-1$
                }
                ResultRenderer renderer = map.remove(key);
                if(renderer != null) {
                    selectRenderPanel.addItem(renderer);
                } else {
                    log.warn("Missing (check spelling error in renderer name) or already added(check doublon) " +
                            "result renderer, check property 'view.results.tree.renderers_order', renderer name:'"+key+"'");
                }
            }
        }
        // Add remaining (plugins or missed in property)
        for (ResultRenderer renderer : map.values()) {
            selectRenderPanel.addItem(renderer);
        }
        nodesModel.setSelectedItem(textObject); // preset to "Text" option
        return selectRenderPanel;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        if (COMBO_CHANGE_COMMAND.equals(command)) {
            JComboBox<?> jcb = (JComboBox<?>) event.getSource();

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
                    log.debug("selectedTab=" + selectedTab);
                    resultsRender.init();
                    // To display current sampler result before change
                    this.valueChanged(lastSelectionEvent);
                }
            }
        }
    }

    public static String getResponseAsString(SampleResult res) {
        String response = null;
        if (isTextDataType(res)) {
            // Showing large strings can be VERY costly, so we will avoid
            // doing so if the response
            // data is larger than 200K. TODO: instead, we could delay doing
            // the result.setText
            // call until the user chooses the "Response data" tab. Plus we
            // could warn the user
            // if this happens and revert the choice if he doesn't confirm
            // he's ready to wait.
            int len = res.getResponseDataAsString().length();
            if (MAX_DISPLAY_SIZE > 0 && len > MAX_DISPLAY_SIZE) {
                StringBuilder builder = new StringBuilder(MAX_DISPLAY_SIZE+100);
                builder.append(JMeterUtils.getResString("view_results_response_too_large_message")) //$NON-NLS-1$
                    .append(len).append(" > Max: ").append(MAX_DISPLAY_SIZE)
                    .append(", ").append(JMeterUtils.getResString("view_results_response_partial_message")) // $NON-NLS-1$
                    .append("\n").append(res.getResponseDataAsString().substring(0, MAX_DISPLAY_SIZE)).append("\n...");
                response = builder.toString();
            } else {
                response = res.getResponseDataAsString();
            }
        }
        return response;
    }

    private static class ResultsNodeRenderer extends DefaultTreeCellRenderer {
        private static final long serialVersionUID = 4159626601097711565L;

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean sel, boolean expanded, boolean leaf, int row, boolean focus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, focus);
            boolean failure = true;
            Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
            if (userObject instanceof SampleResult) {
            	if (userObject instanceof HTTP2SampleResult){
            		HTTP2SampleResult http2Result= (HTTP2SampleResult) userObject;
            		if (http2Result.isPendingResponse()){
            			/* set to a different color the request sent that not get a complete response yet
            			  They will be changed to the corresponding color when response were received  
            			 */
            			this.setForeground(Color.blue);
            			failure=false;
            		} 
            		else{            			
            			 failure = !(http2Result.isSuccessful());
            		}
            	} else {
            		 failure = !(((SampleResult) userObject).isSuccessful());
            	}
               
            } else if (userObject instanceof AssertionResult) {
                AssertionResult assertion = (AssertionResult) userObject;
                failure = assertion.isError() || assertion.isFailure();
            }

            // Set the status for the node
            if (failure) {
                this.setForeground(Color.red);
                this.setIcon(imageFailure);
            } else {
                this.setIcon(imageSuccess);
            }
            
            // Handle search related rendering
            SearchableTreeNode node = (SearchableTreeNode) value;
            if(node.isNodeHasMatched()) {
                setBorder(RED_BORDER);
            } else if (node.isChildrenNodesHaveMatched()) {
                setBorder(BLUE_BORDER);
            } else {
                setBorder(null);
            }
            return this;
        }
    }

    /**
     * Handler for Checkbox
     */
    @Override
    public void itemStateChanged(ItemEvent e) {
        // NOOP state is held by component
    }
    
    
    public DefaultMutableTreeNode findNode(DefaultMutableTreeNode root, int id) {
        Enumeration<DefaultMutableTreeNode> e = root.depthFirstEnumeration();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = e.nextElement();
            if (node.getUserObject() instanceof HTTP2SampleResult){
		        HTTP2SampleResult http2NodeResult= (HTTP2SampleResult) node.getUserObject();
		        if (http2NodeResult.getId()==id) {
		            return node;
		        }
            }
        }
        return null;
    }
    	
    
}
