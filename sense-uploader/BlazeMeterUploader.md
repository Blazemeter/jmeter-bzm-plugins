= BlazeMeter Uploader =

This is special plugin for uploading results to
[https://a.blazemeter.com/ BlazeMeter]
during the test running.

[https://raw.githubusercontent.com/artem-fedorov/jmeter-bzm-plugins/fa136e9c077da3d2419cfa4a3b7cc947229408a1/sense-uploader/blazemeter_uploader.png]

==Fields==

_Anonymous test_ will enable result feeding to service without any other settings required. You will receive the link for your report in the Info Area, and the link will be automatically opened in your system browser. Anonymous reports are kept for 7 days.

_Share test_ if is enabled, anyone with the link can access the report.

_Project Workspace_ field specifies workspace name or Id in which you will upload results.

_Upload to Project_ field specifies project name or Id in which you will upload results.

_Test Title_ may be used to customize test title displayed on report pages.

You need to receive special string token at [https://a.blazemeter.com/ a.blazemeter.com]
to use as your upload identifier with this plugin. You can find it under your [https://a.blazemeter.com/app/#/settings/api-keys Settings => API Keys].
Please, treat the token as confidential, everyone who knows it
may upload files to your projects. Join key ID and secret with single colon: {{{TDknBxu0hmVnJ7NrqtG1F:DFddfgdsljasdfkKSKSDDFKSDFJKSDJFKSDJFsdjfksjfjsSF}}}

==Usage==

  # Go to a.blazemeter.com and [https://a.blazemeter.com/app/#/settings/api-keys issue an upload token]
  # Copy & paste it into _Upload Token_ field of BlazeMeter Uploader
  # Run your test, verify in _Info Area_ that results uploaded successfully
  # Analyze performance report and organize your tests at a.blazemeter.com

Please copy the token carefully, don't miss any chars or add extra.

Make note that BlazeMeter has limits for uploaded tests, projects, workspaces, which differs according to your plan.

