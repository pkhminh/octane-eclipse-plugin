/*******************************************************************************
 * © 2017 EntIT Software LLC, a Micro Focus company, L.P.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.hpe.octane.ideplugins.eclipse.ui.comment;

import java.util.Collection;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.services.util.Util;
import com.hpe.octane.ideplugins.eclipse.ui.comment.job.GetCommentsJob;
import com.hpe.octane.ideplugins.eclipse.ui.comment.job.PostCommentJob;
import com.hpe.octane.ideplugins.eclipse.ui.util.LinkInterceptListener;
import com.hpe.octane.ideplugins.eclipse.ui.util.LoadingComposite;
import com.hpe.octane.ideplugins.eclipse.ui.util.PropagateScrollBrowserFactory;
import com.hpe.octane.ideplugins.eclipse.ui.util.StackLayoutComposite;
import com.hpe.octane.ideplugins.eclipse.util.EntityFieldsConstants;

public class EntityCommentComposite extends StackLayoutComposite {

    private FormToolkit formToolkit = new FormToolkit(Display.getDefault());

    private EntityModel entityModel;

    private Browser commentsBrowser;
    private LoadingComposite loadingComposite;
    private Composite commentsComposite;

    private Text commentText;

    public EntityCommentComposite(Composite parent, int style, EntityModel entityModel) {
        super(parent, style);
        this.entityModel = entityModel;

        loadingComposite = new LoadingComposite(this, SWT.NONE);

        commentsComposite = new Composite(this, SWT.NONE);
        commentsComposite.setLayout(new GridLayout(1, false));
        formToolkit.adapt(commentsComposite);
        formToolkit.paintBordersFor(commentsComposite);

        Label commentsTitleLabel = new Label(commentsComposite, SWT.NONE);
        formToolkit.adapt(commentsTitleLabel, true, true);
        commentsTitleLabel.setText("Comments");
        commentsTitleLabel.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));

        Composite inputCommentAndSendButtonComposite = new Composite(commentsComposite, SWT.NONE);
        inputCommentAndSendButtonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
        formToolkit.adapt(inputCommentAndSendButtonComposite);
        formToolkit.paintBordersFor(inputCommentAndSendButtonComposite);
        inputCommentAndSendButtonComposite.setLayout(new GridLayout(2, false));

        commentText = new Text(inputCommentAndSendButtonComposite, SWT.BORDER);
        commentText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
        commentText.setToolTipText("Add new comment");
        formToolkit.adapt(commentText, true, true);
        commentText.addListener(SWT.Traverse, (Event event) -> {
            if (event.detail == SWT.TRAVERSE_RETURN && commentText.isEnabled()) {
                postComment(commentText.getText());
            }
        });

        Button postCommentBtn = new Button(inputCommentAndSendButtonComposite, SWT.NONE);
        postCommentBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));
        postCommentBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (commentText.isEnabled()) {
                    postComment(commentText.getText());
                    commentText.setEnabled(false);
                }
            }
        });
        formToolkit.adapt(postCommentBtn, true, true);
        postCommentBtn.setText("Post");

        PropagateScrollBrowserFactory browserFactory = new PropagateScrollBrowserFactory();
        commentsBrowser = browserFactory.createBrowser(commentsComposite, SWT.NONE);
        commentsBrowser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        formToolkit.adapt(commentsBrowser);
        formToolkit.paintBordersFor(commentsBrowser);
        showControl(commentsComposite);
        commentsBrowser.setText("<html></html>");
        commentsBrowser.addLocationListener(new LinkInterceptListener());

        // init load
        displayComments();
    }

    private void postComment(String text) {
        commentText.setEnabled(false);
        showControl(loadingComposite);

        PostCommentJob sendCommentJob = new PostCommentJob("Posting Comment", entityModel, text);
        sendCommentJob.schedule();
        sendCommentJob.addJobChangeListener(new JobChangeAdapter() {
            @Override
            public void done(IJobChangeEvent event) {
                Display.getDefault().asyncExec(() -> {
                    if (sendCommentJob.isCommentsSaved()) {
                        displayComments();
                        commentText.setText("");
                        commentText.setEnabled(true);
                    } else {
                        MessageDialog.openError(Display.getCurrent().getActiveShell(), "ERROR",
                                "Comments could not be posted \n ");
                    }
                });
            }
        });
    }

    public void displayComments() {
        GetCommentsJob getCommentsJob = new GetCommentsJob("Getting comments", entityModel);
        getCommentsJob.schedule();
        showControl(loadingComposite);
        getCommentsJob.addJobChangeListener(new JobChangeAdapter() {
            @Override
            public void done(IJobChangeEvent event) {
                Display.getDefault().asyncExec(() -> {
                    String html = getCommentHtmlString(getCommentsJob.getComents());
                    commentsBrowser.setText(html);
                    showControl(commentsComposite);
                });
            }
        });

    }

    private static String getCommentHtmlString(Collection<EntityModel> comments) {
        StringBuilder commentsBuilder = new StringBuilder();
        Color backgroundColor = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry()
                .get(JFacePreferences.CONTENT_ASSIST_BACKGROUND_COLOR);
        String backgroundColorString = "rgb(" + backgroundColor.getRed() + "," + backgroundColor.getGreen() + "," + backgroundColor.getBlue() + ")";

        Color foregroundColor = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry()
                .get(JFacePreferences.CONTENT_ASSIST_FOREGROUND_COLOR);
        String foregroundColorString = "rgb(" + foregroundColor.getRed() + "," + foregroundColor.getGreen() + "," + foregroundColor.getBlue() + ")";
        commentsBuilder.append("<html><body bgcolor =" + backgroundColorString + ">");
        commentsBuilder.append("<font color =" + foregroundColorString + ">");

        if (!comments.isEmpty()) {
            for (EntityModel comment : comments) {
                String commentsPostTime = Util.getUiDataFromModel(comment.getValue(EntityFieldsConstants.FIELD_CREATION_TIME));
                String userName = Util.getUiDataFromModel(comment.getValue(EntityFieldsConstants.FIELD_AUTHOR), "full_name");
                String commentLine = Util.getUiDataFromModel(comment.getValue(EntityFieldsConstants.FIELD_COMMENT_TEXT));
                commentLine = removeHtmlBaseTags(commentLine);
                String currentText = commentsPostTime + " <b>" + userName + ":</b> <br>" + commentLine + "<hr>";
                commentsBuilder.append(currentText);
            }
        }
        commentsBuilder.append("</body></html>");
        return commentsBuilder.toString();
    }

    private static String removeHtmlBaseTags(String htmlString) {
        htmlString = htmlString.replace("<html>", "");
        htmlString = htmlString.replace("<body>", "");
        htmlString = htmlString.replace("</html>", "");
        htmlString = htmlString.replace("</body>", "");
        return htmlString;
    }

}
