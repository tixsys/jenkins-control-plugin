/*
 * Copyright (c) 2011 David Boissier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codinjutsu.tools.jenkins.view;

import org.codinjutsu.tools.jenkins.action.ThreadFunctor;
import org.codinjutsu.tools.jenkins.model.Build;
import org.codinjutsu.tools.jenkins.model.BuildStatusEnum;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.codinjutsu.tools.jenkins.util.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Map.Entry;

import static org.codinjutsu.tools.jenkins.model.BuildStatusEnum.SUCCESS;

public class RssLatestJobPanel implements RssLatestJobView {
    private JPanel rssContentPanel;
    private JPanel rootPanel;

    public RssLatestJobPanel() {
        rssContentPanel.setLayout(new BoxLayout(rssContentPanel, BoxLayout.Y_AXIS));
    }


    public void cleanRssEntries() {
        SwingUtils.runInSwingThread(new ThreadFunctor() {
            public void run() {
                rssContentPanel.invalidate();

                Component[] components = rssContentPanel.getComponents();
                for (Component component : components) {
                    if (component instanceof BuildResultPanel) {
                        rssContentPanel.remove(component);
                    }
                }

                rssContentPanel.validate();
                rssContentPanel.repaint();

            }
        });
    }


    public void addFinishedBuild(final Map<String, Build> stringBuildMap) {
        SwingUtils.runInSwingThread(new ThreadFunctor() {
            public void run() {
                for (Entry<String, Build> entry : stringBuildMap.entrySet()) {
                    addFinishedBuild(entry.getKey(), entry.getValue());
                }

                rssContentPanel.repaint();

            }
        });
    }


    private void addFinishedBuild(String jobName, Build build) {
        String buildMessage = createLinkLabel(build);
        Icon icon = setIcon(build);
        BuildResultPanel buildResultPanel = new BuildResultPanel(jobName, buildMessage, icon, build.getUrl());
        buildResultPanel.getCloseButton()
                .addActionListener(new ClosePanelAction(rssContentPanel, buildResultPanel));
        rssContentPanel.add(buildResultPanel);
    }


    private static String createLinkLabel(Build build) {
        return "Build #" + build.getNumber() + " " + build.getStatusValue();
    }


    private static Icon setIcon(Build build) {
        if (SUCCESS.equals(build.getStatus())) {
            return GuiUtil.loadIcon("accept.png");
        } else if (BuildStatusEnum.ABORTED.equals(build.getStatus())) {
            return GuiUtil.loadIcon("aborted.png");
        }
        return GuiUtil.loadIcon("cancel.png");
    }


    private class ClosePanelAction implements ActionListener {
        private final JPanel parentPanel;
        private final JPanel childPanel;


        private ClosePanelAction(JPanel parentPanel, JPanel childPanel) {
            this.parentPanel = parentPanel;
            this.childPanel = childPanel;
        }


        public void actionPerformed(ActionEvent e) {
            parentPanel.getRootPane().invalidate();
            parentPanel.remove(childPanel);
            parentPanel.getRootPane().validate();
            parentPanel.repaint();
        }
    }
}
