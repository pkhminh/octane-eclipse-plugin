/*******************************************************************************
 * Copyright 2017 Hewlett-Packard Enterprise Development Company, L.P.
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
package com.hpe.octane.ideplugins.eclipse.ui.search;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.hpe.octane.ideplugins.eclipse.util.resource.ImageResources;

public class NoSearchResultsComposite extends Composite {

    public NoSearchResultsComposite(Composite parent, int style) {
        super(parent, style);
        setLayout(new GridLayout(1, false));

        Label lblPlaceholder = new Label(this, SWT.NONE);
        lblPlaceholder.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));

        Label lblUnidragon = new Label(this, SWT.NONE);
        lblUnidragon.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
        lblUnidragon.setImage(ImageResources.UNIDRAG_SMALL_SAD.getImage());

        Label lblNoResults = new Label(this, SWT.NONE);
        lblNoResults.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true, true, 1, 1));
        lblNoResults.setText("No results");
    }

}
