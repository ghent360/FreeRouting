/*
 *  Copyright (C) 2014  Alfons Wirtz  
 *   website www.freerouting.net
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License at <http://www.gnu.org/licenses/> 
 *   for more details.
 *
 * MainApplication.java
 *
 * Created on 19. Oktober 2002, 17:58
 *
 */
package gui;

import board.TestLevel;

/**
 *
 * Main application for creating frames with new or existing board designs.
 *
 * @author  Alfons Wirtz
 */
public class MainApplication extends javax.swing.JFrame
{

    /**
     * Main function of the Application
     */
    public static void main(String p_args[])
    {
        boolean designFileAsParameter = false;
        boolean test_version_option = false;
        boolean autoSaveSpectraSessionFileOnExit = false;
        String design_file_name = null;
        String design_dir_name = null;
        java.util.Locale current_locale = java.util.Locale.ENGLISH;
        for (int i = 0; i < p_args.length; ++i)
        {
            if (p_args[i].startsWith("-de"))
            // the design file is provided
            {
                if (p_args.length > i + 1 && !p_args[i + 1].startsWith("-"))
                {
                    designFileAsParameter = true;
                    design_file_name = p_args[i + 1];
                }
            }
            else if (p_args[i].startsWith("-di"))
            // the design directory is provided
            {
                if (p_args.length > i + 1 && !p_args[i + 1].startsWith("-"))
                {
                    design_dir_name = p_args[i + 1];
                }
            }
            else if (p_args[i].startsWith("-l"))
            // the locale is provided
            {
                if (p_args.length > i + 1 && p_args[i + 1].startsWith("d"))
                {
                    current_locale = java.util.Locale.GERMAN;
                }
            }
            else if (p_args[i].startsWith("-s"))
            {
                autoSaveSpectraSessionFileOnExit = true;
            }
            else if (p_args[i].startsWith("-test"))
            {
                test_version_option = true;
            }
            else if (p_args[i].startsWith("-h")||p_args[i].startsWith("--help"))
            {
                System.out.println("FreeRouting version "+VERSION_NUMBER_STRING);
                System.out.println("command line options are:");
                System.out.println("-de  provide design file");
                System.out.println("-di  design folder used in file dialog");
                System.out.println("-l   provide locale");
                System.out.println("-s   spectra session file is automatic saved on exit");
                System.out.println("-t   test option");
                System.out.println("-h   this help");
                return;
            }
        }

        java.util.ResourceBundle resources = java.util.ResourceBundle.getBundle("gui.resources.MainApplication", current_locale);
        if (designFileAsParameter)
        {

            DesignFile design_file = DesignFile.get_instance(design_file_name);
            if (design_file == null)
            {
                System.out.print(resources.getString("message_6") + " ");
                System.out.print(design_file_name);
                System.out.println(" " + resources.getString("message_7"));
                return;
            }
            String message = resources.getString("loading_design") + " " + design_file_name;
            WindowMessage welcome_window = WindowMessage.show(message);
            final BoardFrame new_frame =
                    create_board_frame(design_file, autoSaveSpectraSessionFileOnExit, test_version_option, current_locale);
            welcome_window.dispose();
            if (new_frame == null)
            {
                System.out.print(resources.getString("message_6") + " ");
                System.out.print(design_file_name);
                System.out.println(" " + resources.getString("message_7"));
                Runtime.getRuntime().exit(1);
            }
            new_frame.addWindowListener(new java.awt.event.WindowAdapter()
            {
                public void windowClosed(java.awt.event.WindowEvent evt)
                {
                    Runtime.getRuntime().exit(0);
                }
            });
        }
        else
        {

            DesignFile design_file = DesignFile.open_dialog(design_dir_name);

            if (design_file == null)
            {
                String message1 = resources.getString("message_3")+" ";
                int option = javax.swing.JOptionPane.showConfirmDialog(null, message1 , "error", 0,javax.swing.JOptionPane.ERROR_MESSAGE);
                Runtime.getRuntime().exit(1);
            }

            String message = resources.getString("loading_design") + " " + design_file.get_name();
            WindowMessage welcome_window = WindowMessage.show(message);
            welcome_window.setTitle(message);
            BoardFrame new_frame =
                    create_board_frame(design_file, autoSaveSpectraSessionFileOnExit, test_version_option, current_locale);
            welcome_window.dispose();
            if (new_frame == null)
            {
                Runtime.getRuntime().exit(1);
            }
            new_frame.addWindowListener(new java.awt.event.WindowAdapter()
            {
                public void windowClosed(java.awt.event.WindowEvent evt)
                {
                    Runtime.getRuntime().exit(0);
                }
            });

        }
    }



    /**
     * Creates a new board frame containing the data of the input design file.
     * Returns null, if an error occured.
     */
    static private BoardFrame create_board_frame(DesignFile p_design_file,
            boolean autoSaveSpectraSessionFileOnExit, boolean p_is_test_version, java.util.Locale p_locale)
    {
        java.util.ResourceBundle resources =
                java.util.ResourceBundle.getBundle("gui.resources.MainApplication", p_locale);

        java.io.InputStream input_stream = p_design_file.get_input_stream();
        if (input_stream == null) return null;


        TestLevel test_level;
        if (p_is_test_version)
        {
            test_level = DEBUG_LEVEL;
        }
        else
        {
            test_level = TestLevel.RELEASE_VERSION;
        }
        BoardFrame new_frame = new BoardFrame(p_design_file, autoSaveSpectraSessionFileOnExit, test_level, p_locale);
        boolean read_ok = new_frame.read(input_stream, p_design_file.is_created_from_text_file(), null);
        if (!read_ok)
        {
            return null;
        }
        //new_frame.menubar.add_design_dependent_items();
        if (p_design_file.is_created_from_text_file())
        {
            // Read the file  with the saved rules, if it is existing.

            String file_name = p_design_file.get_name();
            String[] name_parts = file_name.split("\\.");
            String confirm_import_rules_message = resources.getString("confirm_import_rules");
            DesignFile.read_rules_file(name_parts[0], p_design_file.get_parent(),
                    new_frame.board_panel.board_handling,
                    confirm_import_rules_message);
            new_frame.refresh_windows();
        }
        return new_frame;
    }


    public MainApplication(){
    }




    /** The list of open board frames */
    private String design_dir_name = null;
    private static final TestLevel DEBUG_LEVEL = TestLevel.CRITICAL_DEBUGGING_OUTPUT;

    /**
     * Change this string when creating a new version
     */
    static final String VERSION_NUMBER_STRING = "1.2.44";
}
