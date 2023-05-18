package be.orbinson.aem.sdk.dev.tools.asset.workflow.hook;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.vault.packaging.InstallContext;
import org.apache.jackrabbit.vault.packaging.InstallHook;
import org.apache.jackrabbit.vault.packaging.PackageException;
import org.apache.jackrabbit.vault.packaging.PackageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

public class DisableAssetWorkflowsHook implements InstallHook {
    private static final Logger log = LoggerFactory.getLogger(DisableAssetWorkflowsHook.class);

    private static final String PACKAGE_PROPERTY_EXTRA_WORKFLOWS = "extraAssetWorkflows";
    private static final String PACKAGE_PROPERTY_AEM_VERSION = "aemVersion";
    private static final String DEFAULT_AEM_VERSION = "cloud";

    private static final String[] CLOUD_ASSET_WORKFLOWS = {
            "/libs/settings/workflow/launcher/config/asset_processing_on_sdk_create",
            "/libs/settings/workflow/launcher/config/asset_processing_on_sdk_mod",
            "/libs/settings/workflow/launcher/config/dam_xmp_writeback",
            "/libs/settings/workflow/launcher/config/metadata_set_lastmodified"
    };

    private static final String[] AEM_6_5_ASSET_WORKFLOWS = {
            "/conf/global/settings/workflow/launcher/config/dam_xmp_writeback",
            "/libs/settings/workflow/launcher/config/dam_xmp_writeback",
            "/conf/global/settings/workflow/launcher/config/metadata_set_lastmodified",
            "/libs/settings/workflow/launcher/config/metadata_set_lastmodified",
            "/conf/global/settings/workflow/launcher/config/update_asset_create",
            "/libs/settings/workflow/launcher/config/update_asset_create",
            "/conf/global/settings/workflow/launcher/config/update_asset_create_without_DM",
            "/libs/settings/workflow/launcher/config/update_asset_create_without_DM",
            "/conf/global/settings/workflow/launcher/config/update_asset_mod",
            "/libs/settings/workflow/launcher/config/update_asset_mod",
            "/conf/global/settings/workflow/launcher/config/update_asset_mod_without_DM",
            "/libs/settings/workflow/launcher/config/update_asset_mod_without_DM",
            "/conf/global/settings/workflow/launcher/config/update_asset_mod_reupload",
            "/libs/settings/workflow/launcher/config/update_asset_mod_reupload"
    };

    public void execute(InstallContext context) throws PackageException {
        try {
            switch (context.getPhase()) {
                case PREPARE:
                    disableAssetWorkflowsStatus(context.getSession(), context.getPackage().getProperties());
                    break;
                case END:
                    enableAssetWorkflowsStatus(context.getSession(), context.getPackage().getProperties());
                    break;
            }
        } catch (Exception e) {
            log.error("Could not set the asset workflows status, skipping the install hook", e);
        }
    }

    private void disableAssetWorkflowsStatus(Session session, PackageProperties packageProperties) throws RepositoryException {
        setAssetWorkflowsStatus(session, packageProperties, false);
    }

    private void enableAssetWorkflowsStatus(Session session, PackageProperties packageProperties) throws RepositoryException {
        setAssetWorkflowsStatus(session, packageProperties, true);
    }

    private void setAssetWorkflowsStatus(Session session, PackageProperties packageProperties, boolean enabled) throws RepositoryException {
        log.debug("Setting asset workflows status to {}", enabled);
        String aemVersion = packageProperties.getProperty(PACKAGE_PROPERTY_AEM_VERSION) == null ? DEFAULT_AEM_VERSION : packageProperties.getProperty(PACKAGE_PROPERTY_AEM_VERSION);
        if (aemVersion.equals(DEFAULT_AEM_VERSION)) {
            for (String assetWorkflow : CLOUD_ASSET_WORKFLOWS) {
                setWorkflowStatus(session, assetWorkflow, enabled);
            }
        } else {
            for (String assetWorkflow : AEM_6_5_ASSET_WORKFLOWS) {
                setWorkflowStatus(session, assetWorkflow, enabled);
            }
        }
        setExtraAssetWorkflowsStatus(session, packageProperties, enabled);
        session.save();
    }

    private void setExtraAssetWorkflowsStatus(Session session, PackageProperties packageProperties, boolean enabled) throws RepositoryException {
        String extraAssetWorkflowsProperty = packageProperties.getProperty(PACKAGE_PROPERTY_EXTRA_WORKFLOWS);
        if (StringUtils.isNotBlank(extraAssetWorkflowsProperty)) {
            String[] extraAssetWorkflows = extraAssetWorkflowsProperty.split(",");
            for (String extraAssetWorkflow : extraAssetWorkflows) {
                setWorkflowStatus(session, extraAssetWorkflow, enabled);
            }
        }
    }

    void setWorkflowStatus(Session session, String nodePath, boolean enabled) throws RepositoryException {
        if (session.nodeExists(nodePath)) {
            if (hasCapabilityToEdit(session, nodePath)) {
                log.debug("Setting workflow <{}> to enabled <{}>", nodePath, enabled);
                Node node = session.getNode(nodePath);
                node.setProperty("enabled", enabled);
            } else {
                log.debug("Skipping workflow <{}> to enabled <{}> because in immutable node store", nodePath, enabled);
            }
        }
    }

    private boolean hasCapabilityToEdit(Session session, String nodePath) throws RepositoryException {
        if (nodePath.startsWith("/apps") || nodePath.startsWith("/libs")) {
            Node appsNode = session.getNode("/apps");
            return session.hasCapability("addNode", appsNode, new Object[]{"nt:folder"});
        } else {
            return true;
        }
    }
}



