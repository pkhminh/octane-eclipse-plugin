package com.hpe.octane.ideplugins.eclipse.ui.search;

import static com.hpe.adm.octane.services.util.Util.getUiDataFromModel;

import java.net.MalformedURLException;
import java.net.URI;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.nga.sdk.model.ReferenceFieldModel;
import com.hpe.adm.octane.services.EntityService;
import com.hpe.adm.octane.services.filtering.Entity;
import com.hpe.adm.octane.services.mywork.MyWorkService;
import com.hpe.adm.octane.services.util.UrlParser;
import com.hpe.adm.octane.services.util.Util;
import com.hpe.octane.ideplugins.eclipse.Activator;
import com.hpe.octane.ideplugins.eclipse.ui.editor.EntityModelEditor;
import com.hpe.octane.ideplugins.eclipse.ui.editor.EntityModelEditorInput;
import com.hpe.octane.ideplugins.eclipse.ui.entitylist.EntityModelMenuFactory;
import com.hpe.octane.ideplugins.eclipse.util.DebugUtil;
import com.hpe.octane.ideplugins.eclipse.util.EntityIconFactory;
import com.hpe.octane.ideplugins.eclipse.util.resource.ImageResources;

public class SearchEntityModelMenuFactory implements EntityModelMenuFactory {

    // private static final ILog logger = Activator.getDefault().getLog();

    private static final EntityIconFactory entityIconFactory = new EntityIconFactory(16, 16, 7);
    private static EntityService entityService = DebugUtil.serviceModule.getInstance(EntityService.class);
    private static MyWorkService myWorkService = DebugUtil.serviceModule.getInstance(MyWorkService.class);
    private ViewPart parentViewPart;

    public SearchEntityModelMenuFactory(ViewPart parentViewPart) {
        this.parentViewPart = parentViewPart;
    }

    private void openDetailTab(Integer entityId, Entity entityType) {
        EntityModelEditorInput entityModelEditorInput = new EntityModelEditorInput(entityId, entityType);
        try {
            parentViewPart.getSite().getWorkbenchWindow().getActivePage().openEditor(entityModelEditorInput, EntityModelEditor.ID);
        } catch (PartInitException ex) {
            // logger.log(new Status(Status.ERROR, Activator.PLUGIN_ID,
            // Status.ERROR, "An exception has occured when opening the editor",
            // ex));
        }
    }

    @Override
    public Menu createMenu(EntityModel entityModel, Control menuParent) {

        Menu menu = new Menu(menuParent);

        Entity entityType = Entity.getEntityType(entityModel);
        Integer entityId = Integer.valueOf(getUiDataFromModel(entityModel.getValue("id")));

        addMenuItem(
                menu,
                "View in browser (System)",
                ImageResources.BROWSER_16X16.getImage(),
                () -> entityService.openInBrowser(entityModel));

        if (PlatformUI.getWorkbench().getBrowserSupport().isInternalWebBrowserAvailable()) {
            addMenuItem(
                    menu,
                    "View in browser (Eclipse)",
                    ImageResources.BROWSER_16X16.getImage(),
                    () -> {
                        Entity ownerEntityType = null;
                        Integer ownerEntityId = null;
                        if (entityType == Entity.COMMENT) {
                            ReferenceFieldModel owner = (ReferenceFieldModel) Util.getContainerItemForCommentModel(entityModel);
                            ownerEntityType = Entity.getEntityType(owner.getValue());
                            ownerEntityId = Integer.valueOf(Util.getUiDataFromModel(owner, "id"));
                        }
                        URI uri = UrlParser.createEntityWebURI(
                                Activator.getConnectionSettings(),
                                entityType == Entity.COMMENT ? ownerEntityType : entityType,
                                entityType == Entity.COMMENT ? ownerEntityId : entityId);
                        try {
                            PlatformUI.getWorkbench().getBrowserSupport().createBrowser(uri.toString()).openURL((uri.toURL()));
                        } catch (PartInitException | MalformedURLException e) {
                            e.printStackTrace();
                        }
                    });
        }

        new MenuItem(menu, SWT.SEPARATOR);

        if (entityType != Entity.COMMENT) {
            addMenuItem(
                    menu,
                    "View details",
                    entityIconFactory.getImageIcon(entityType),
                    () -> openDetailTab(entityId, entityType));
        }

        if (myWorkService.isAddingToMyWorkSupported(entityType)) {
            new MenuItem(menu, SWT.SEPARATOR);
            addMenuItem(
                    menu,
                    "Add to \"My Work\"",
                    ImageResources.ADD.getImage(),
                    () -> {
                        menuParent.getDisplay().asyncExec(() -> {
                            if (myWorkService.addToMyWork(entityModel)) {
                                // TODO: Show added notification
                            } else {
                                // TODO: Show add failed notification
                            }
                        });

                    });
        }

        return menu;
    }

    private static MenuItem addMenuItem(Menu menu, String text, Image image, Runnable selectAction) {
        MenuItem menuItem = new MenuItem(menu, SWT.NONE);
        if (image != null) {
            menuItem.setImage(image);
        }
        menuItem.setText(text);
        menuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                selectAction.run();
            }
        });
        return menuItem;
    }

}
