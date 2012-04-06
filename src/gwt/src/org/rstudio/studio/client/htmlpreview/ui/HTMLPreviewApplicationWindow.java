/*
 * PDFViewerApplicationWindow.java
 *
 * Copyright (C) 2009-11 by RStudio, Inc.
 *
 * This program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */
package org.rstudio.studio.client.htmlpreview.ui;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.rstudio.studio.client.application.events.EventBus;
import org.rstudio.studio.client.common.satellite.SatelliteWindow;
import org.rstudio.studio.client.htmlpreview.HTMLPreviewPresenter;
import org.rstudio.studio.client.htmlpreview.model.HTMLPreviewParams;
import org.rstudio.studio.client.workbench.ui.FontSizeManager;

@Singleton
public class HTMLPreviewApplicationWindow extends SatelliteWindow
                                          implements HTMLPreviewApplicationView
{

   @Inject
   public HTMLPreviewApplicationWindow(Provider<HTMLPreviewPresenter> pPresenter,
                                       Provider<EventBus> pEventBus,
                                       Provider<FontSizeManager> pFSManager)
   {
      super(pEventBus, pFSManager);
      
      pPresenter_ = pPresenter; 
     
   }

   @Override
   protected void onInitialize(LayoutPanel mainPanel, JavaScriptObject params)
   {
      Window.setTitle("RStudio: Preview HTML");
      
      // create the presenter and activate it with the passed params
      HTMLPreviewParams htmlPreviewParams = params.<HTMLPreviewParams>cast();
      presenter_ = pPresenter_.get();
      presenter_.onActivated(htmlPreviewParams);
      
      // make it fill the containing layout panel
      Widget presWidget = presenter_.asWidget();
      presWidget.setSize("100%", "100%");
      mainPanel.setSize("100%", "100%");
      mainPanel.add(presWidget);
      mainPanel.setWidgetLeftRight(presWidget, 0, Unit.PX, 0, Unit.PX);
      mainPanel.setWidgetTopBottom(presWidget, 0, Unit.PX, 0, Unit.PX);
   }

   @Override
   public void reactivate(JavaScriptObject params)
   {
      if (params != null)
      {
         HTMLPreviewParams htmlPreviewParams = params.<HTMLPreviewParams>cast();
         presenter_.onActivated(htmlPreviewParams);
      }
   }
   
   @Override 
   public Widget getWidget()
   {
      return this;
   }

   private final Provider<HTMLPreviewPresenter> pPresenter_;
   private HTMLPreviewPresenter presenter_;

}