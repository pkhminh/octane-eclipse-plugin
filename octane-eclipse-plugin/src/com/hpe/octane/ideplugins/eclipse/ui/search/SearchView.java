package com.hpe.octane.ideplugins.eclipse.ui.search;

import org.eclipse.core.runtime.ILog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.hpe.octane.ideplugins.eclipse.Activator;
import com.hpe.octane.ideplugins.eclipse.ui.OctaneViewPart;
import com.hpe.octane.ideplugins.eclipse.ui.util.ErrorComposite;
import com.hpe.octane.ideplugins.eclipse.ui.util.NoSearchResultsComposite;

public class SearchView extends OctaneViewPart {

    private static final ILog logger = Activator.getDefault().getLog();

    public static final String ID = "com.hpe.octane.ideplugins.eclipse.ui.SearchView";

    /**
     * Shown when my work service returns an empty list
     */
    private NoSearchResultsComposite noSearchResultsComposite;
    private ErrorComposite errorComposite;

    @Override
    public Control createOctanePartControl(Composite parent) {
        noSearchResultsComposite = new NoSearchResultsComposite(parent, SWT.NONE);
        errorComposite = new ErrorComposite(parent, SWT.NONE);

        showControl(noSearchResultsComposite);

        // Init
        if (Activator.getConnectionSettings().isEmpty()) {
            showWelcome();
        }

        // Return root
        Label lbl = new Label(parent, SWT.NONE);
        lbl.setText("Placeholder");
        return lbl;

    }

    @Override
    public void setFocus() {
        // TODO Auto-generated method stub
    }

}
