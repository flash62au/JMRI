package apps.swing;

import java.util.*;

import javax.annotation.Nonnull;
import javax.swing.JMenuItem;
import javax.swing.UIManager;

import apps.*;
import apps.plaf.macosx.Application;

import jmri.jmrit.XmlFileLocationAction;
import jmri.util.*;

import org.openide.util.lookup.ServiceProvider;

import apps.util.issuereporter.swing.IssueReporterAction;

/**
 * Common utility methods for working with Java Help.
 * <p>
 * This class was created to contain common Java Help information.
 * <p>
 * It assumes that Java Help 1.1.8 is in use
 *
 * @author Bob Jacobsen Copyright 2007
 */
@ServiceProvider(service = HelpUtil.MenuProvider.class)
public class HelpMenuProvider implements HelpUtil.MenuProvider {

    public HelpMenuProvider() {
        // do nothing
    }

    @Nonnull
    @Override
    public List<JMenuItem> getHelpMenuItems() {
        List<JMenuItem> items = new ArrayList<>();

        JMenuItem item = new JMenuItem(Bundle.getMessage("MenuItemHelp"));
        HelpUtil.enableHelpOnButton(item, "index");
        items.add(item);

        JMenuItem license = new JMenuItem(Bundle.getMessage("MenuItemLicense"));
        items.add(license);
        license.addActionListener(new LicenseAction());

        JMenuItem directories = new JMenuItem(Bundle.getMessage("MenuItemFileLocations"));
        items.add(directories);
        directories.addActionListener(new XmlFileLocationAction());

        JMenuItem updates = new JMenuItem(Bundle.getMessage("MenuItemCheckUpdates"));
        items.add(updates);
        updates.addActionListener(new CheckForUpdateAction());

        JMenuItem context = new JMenuItem(Bundle.getMessage("MenuItemContext"));
        items.add(context);
        context.addActionListener(new ReportContextAction());

        JMenuItem console = new JMenuItem(Bundle.getMessage("MenuItemConsole"));
        items.add(console);
        console.addActionListener(new SystemConsoleAction());

        items.add(new JMenuItem(new IssueReporterAction()));

        JMenuItem jmriusers = new JMenuItem(Bundle.getMessage("MenuItemJmriUsers"));
        items.add(jmriusers);
        jmriusers.addActionListener(new JmriUsersAction());

        // Put about dialog in Apple's preferred area on Mac OS X
        if (SystemType.isMacOSX()) {
            try {
                Objects.requireNonNull(Application.getApplication()).setAboutHandler((EventObject eo) ->
                        new AboutDialog(null, true).setVisible(true));
            } catch (java.lang.RuntimeException re) {
                log.error("Unable to put About handler in default location", re);
            }
        }
        // Include About in Help menu if not on Mac OS X or not using Aqua Look and Feel
        if (!SystemType.isMacOSX() || !UIManager.getLookAndFeel().isNativeLookAndFeel()) {
            items.add(null);
            JMenuItem about = new JMenuItem(Bundle.getMessage("MenuItemAbout") + " " + jmri.Application.getApplicationName());
            items.add(about);
            about.addActionListener(new AboutAction());
        }
        return items;
    }

    // initialize logging
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HelpMenuProvider.class);
}
