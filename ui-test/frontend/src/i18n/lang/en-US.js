import el from "metersphere-frontend/src/i18n/lang/ele-en-US";
import fu from "fit2cloud-ui/src/locale/lang/en_US"; // 加载fit2cloud的内容
import mf from "metersphere-frontend/src/i18n/lang/en-US"

const message = {}
export default {
    ...el,
    ...fu,
    ...mf,
    ...message,
    ui: {
        ui_automation: "UI Automation",
        ui_element: "UI Element Library",
        report: "Test Report",
        ui_debug_mode: 'UI debugging mode',
        ui_local_debug: 'local',
        ui_server_debug: 'server',
        all_element: "All elements",
        element_name: "Element name",
        element_locator: "Element locator",
        element_locator_type: "Locator type",
        screenshot: "screenshot",
        update_user: "Update user",
        create_user: "Create user",
        all_scenario: "All scenario",
        scenario_list: "Scenario list",
        log: "log",
        performance_mode: "Performance mode",
        error_handling: "error handling",
        other_settings: "Other settings",
        step_results: "Step results",
        treatment_method: "Treatment method",
        scenario_steps: "Scenario steps",
        cmdValidation: "Validation",
        cmdValidateValue: "ValidateValue",
        cmdValidateText: "ValidateText",
        cmdValidateDropdown: "ValidateDropdown",
        cmdValidateElement: "ValidateElement",
        cmdValidateTitle: "ValidateTitle",
        cmdOpen: "Open",
        cmdSelectWindow: "SelectWindow",
        cmdSetWindowSize: "SetWindowSize",
        cmdSelectFrame: "SelectFrame",
        cmdDialog: "DialogOperation",
        cmdDropdownBox: "DropdownBox",
        submit: "Submit",
        cmdSetItem: "SetItem",
        cmdWaitElement: "WaitElement",
        cmdInput: "Input",
        cmdMouseClick: "MouseClick",
        cmdMouseMove: "MouseMove",
        cmdMouseDrag: "MouseDrag",
        cmdTimes: "Times",
        cmdForEach: "ForEach",
        cmdWhile: "While",
        cmdIf: "If",
        cmdElse: "Else",
        cmdElseIf: "ElseIf",
        close: "Close",
        cmdExtraction: "Extraction",
        cmdExtractWindow: "ExtractWindow",
        cmdExtractElement: "ExtractElement",
        valiate_fail: "Validate fail",
        basic_information: "Basic information",
        step_type: "Step type",
        selenium_tip: "Support Selenium-IDE plugin format import",
        selenium_export_tip: "Export side file via MeterSphere",
        elementObject: "Element Object",
        elementLocator: "Element Locator",
        elementType: "Category",
        not_selected: "(No element selected)",
        not_selected_location: "(No selected element location)",
        location: "Location",
        run: "Run",
        locate_type: "Location method",
        coord: "coordinate",
        enable_or_not: "Enable/Disable",
        enable: "Enable",
        disable: "Disable",
        resolution: "resolution",
        ignore_fail: "Ignore exception and continue execution",
        not_ignore_fail: "Abort process",
        input_or_not: "input",
        input_content: "Input content",
        insert_content: "Type content",
        append_content: "Append input",
        append_tip: "Check, append the input after the existing content;Uncheck, clear the existing content and then input",
        pls_input: "Please input content",
        opt_type: "mode",
        yes: "Yes",
        no: "No",
        confirm: "OK",
        cancel: "Cancel",
        press_button: "Click the popup OK button or Cancel button",
        param_null: "Parameter cannot be null",
        operation_object: "Operation object",
        sub_item: "Sub-item",
        value: "value",
        select: "Select",
        option: "Option ( Option )",
        index: "Index ( Index )",
        s_value: "Value ( Value )",
        text: "Text ( Text )",
        set_itera: "Set traversal",
        foreach_tip: "Set loop iteration, support array row data, for example: [1,2,3]; you can also enter variables",
        intervals: "Interval time",
        condition_type: "Condition Type",
        please_select: "Please select",
        condition_list: "Condition list: set multiple conditions by list",
        condition_list_: "Condition List",
        condition_exp: "Conditional expression: If the expression is true, then execute the steps inside",
        condition_exp_: "Condition expression",
        expression: "expression",
        if_tip: "Please use ${var} for variables, and single quotes for strings, such as: ${name} === 'Zhangsan'",
        input_c_tip: "The contenteditable attribute of an editable paragraph element must be true to enable input; for example:  contenteditable='true' This is an editable paragraph. Please try editing the text.",
        input: "input box",
        editable_p: "Editable paragraph",
        click_type: "Click method",
        set_click_point: "Set the mouse click position",
        click_tip_1: "Check to control the click position of the mouse on the element",
        element_location: "Element Location",
        click_point: "Click position",
        x: "Abscissa",
        y: "ordinate",
        click_tip_2: "The upper left corner of the default element is 0, 0; by setting the relative position, control the click position of the mouse on the element",
        click: "click",
        dclick: "Double click",
        press: "press",
        standup: "Bounce up",
        mouse_start: "Mouse start position",
        drag_start: "The position of the starting point of the dragged element",
        mouse_end: "Mouse end position",
        drag_end: "The final position of the dragged element",
        move_type: "Move Type",
        mouse_location: "Mouse location",
        relative_location: "relative coordinate location",
        move_tip: "Relative position, the current position coordinate of the element is 0, 0",
        mouse_out_e: "Mouse out of element",
        mouse_in_e: "Mouse in element",
        mouse_e_to_c: "Mouse mouse from element to coordinate position",
        url: "URL or relative path",
        sf_tip: "If you are switching frames, you need to pass in the index or element positioning before switching",
        sf_index: "frame index number",
        select_index: "Select the frame of the current page;",
        select_f_tip: "Example: For example, if the index value is entered as 1, the effect will switch to the second frame of the current page (the index value starts from 0)",
        exit_frame: "Exit the current frame (back to the main page)",
        select_frame_index: "Switch to the specified frame according to the frame index",
        select_by_location: "Switch frame according to the positioning method",
        sw_tip1: "If you switch to the specified window, you need to pass in the handle",
        handle_id: "Handle ID",
        window_handle: "Window handle ID",
        frame_index: "Webpage index number",
        window_index: "Window web page index number",
        select_open_window: "Select the number of pages that have been opened;",
        s_w_t1: "Example: For example, if the index value is entered as 3, then the effect will switch to the third window that has been opened (the index value starts from 1)",
        switch_by_id: "Switch to the specified window according to the handle ID",
        switch_by_index: "Switch to the specified window according to the page index number",
        switch_to_default: "Switch to initial window",
        ws_tip1: "Specify the size, set the size of the window according to the input width and height",
        size: "Size:",
        by_pixel: "in pixels",
        width: "width",
        height: "Height",
        times: "Number of cycles",
        set_times: "Set the number of times of the loop, you can enter a variable",
        wait_text: "Wait for text",
        wait_timeout: "Wait Timeout",
        wait_for_text: "Wait for the element to be equal to the given value (Text)",
        wait_for_ele_pre: "Wait for element to exist",
        wait_for_ele_visible: "Wait for element to show",
        wait_for_ele_not_visible: "Wait for element not visible",
        wait_for_ele_not_pre: "Wait for element not present",
        wait_for_ele_edi: "Wait for element to be editable",
        wait_for_ele_not_edi: "Wait for element not editable",
        wait_tip: "For the Text attribute of the element, it refers to the text content displayed on the page, and the waiting timeout time is 15000ms",
        exe_first: "Execute first and then judge",
        while_t_1: "Execute first and then judge similar to doWhile , execute the loop body once and then judge the condition",
        while_t_2: "Please use ${var} for variables and single quotes for strings, such as: ${name} === 'Zhangsan'",
        loop_time_out: "Loop timeout",
        operation: "Operation",
        use_pixel: 'use pixel',
        fullscreen: 'maximum',
        swicth_to_default: "switch to origin window",
        program_controller: 'Process control',
        input_operation: 'input operation',
        mouse_operation: 'Mouse operation',
        element_operation: 'Element operation',
        dialog_operation: 'Pop-up operation',
        browser_operation: 'Browser operation',
        pause: 'Pause',
        browser: "Browser",
        inner_drag: "Drag in element",
        set_coord: "Set Coordinate",
        input_content_x: "input x coordinate",
        input_content_y: "input y coordinate",
        screenshot_config: "Screenshot configuration",
        current_step_screenshot: "Screenshot of current step",
        not_screentshot: "No screenshot",
        error_step_screenshot: "Screenshot of exception",
        downloadScreenshot: "Download screenshot file",
        description: "remark",
        scenario_title: "Scenario",
        custom_command_title: "Command",
        custom_command_label: "Custom command",
        automation_list: "Automation list",
        create_custom_command: "Add command",
        create_custom_command_label: "Create custom command",
        import_by_list_label: "UI list import",
        open_custom_command_label: "Open command",
        debug_result_label: "Debug result",
        scenario_ref_label: "Scenario reference",
        command_ref_label: "Command reference",
        test_plan_ref_label: "Test plan reference",
        delete_scenario_lable: "Delete scenario",
        delete_command_lable: "Delete command",
        command_name_label: "Command name",
        default_module: "Default module",
        executing: "Executing...",
        unexecute: "PENDING",
        check_command: "Please tick the instruction",
        ip: "ip address",
        cword: "Word",
        csentence: "Sentence",
        cparagraph: "Paragraph",
        loading: "Loading...",
        close_dialog: "close",
        unknown_scenario: "Unknown Scenario",
        unknown_instruction: "Unknown Instruction",
        unknown_element: "Unknown Element",
        scenario_ref_add_warning: "No other steps can be added to the referenced scene/instruction steps and sub steps!",
        batch_editing_steps: "Batch editing steps",
        wait_time_config: "Timeout setting",
        wait_element_timeout: "Wait element timeout",
        more_config_options: "More advanced settings options",
        updated_config_info: "The updated options are",
        config_success: "Config success",
        cmdFileUpload: "File　upload",
        relevant_file: "Relevant File",
        validate_tips: "To judge whether the actual result is consistent with the expected one, you can add multiple assertions",
        instruction: "instruction",
        screen_tip: "If the scene step triggers a native popup (alert or prompt), or if there is no page, the screenshot will not take effect;",
        ele_css_attr: "Element CSS attribute",
        ele_css_tip1: "Such as element CSS properties, color properties, font-size properties, etc.",
        store_css_attr: "CSS attribute (storeCssAttribute)",
        validate_type: "Please select an assertion method",
        expect_value: "Expected value",
        expect_title: "Please enter the desired page title",
        title_tip: "Assert whether the title of the current window is consistent with the expected value, if it matches exactly, the assertion succeeds, otherwise it fails",
        input_var: "Please enter a variable",
        input_expect: "Please enter the expected value",
        var_tip: "Assert whether the variable matches the expected value",
        confirm_after_success: "Whether to click the confirm button after success",
        input_expect_text: "Please enter the expected popup text",
        input_window_tip: "Only supports the assertion of pop-up text. If yes, the confirmation button on the pop-up will be clicked after the assertion is successful. If no, no button will be clicked after the assertion is successful",
        select_value: "The value of the selected element is equal to the desired (SelectedValue)",
        select_label: "The text displayed by the drop-down box option is equal to the expected (SelectedLabel) ",
        not_select_value: "The value of the selected element is not equal to the expected (NotSelectedValue)",
        assert_check: "The element is checked (Checked)",
        assert_editable: "Element is editable (Editable)",
        assert_element_present: "Element Present (ElementPresent)",
        assert_element_not_present: "ElementNotPresent",
        assert_element_not_checked: "Element is not checked (NotChecked)",
        assert_element_not_editable: "Element is not editable (NotEditable)",
        assert_element_not_text: "Element text is not equal to expected (NotText)",
        assert_text: "Element text equals expected(Text)",
        assert_value: "The element value is equal to the expected (Value)",
        script_tip: "Only js script is supported, the set script will be executed in the browser",
        script_type: "Script Type",
        set_var: "Set variable",
        async: "async",
        sync: "Sync",
        return: "There is a return value",
        no_return: "No return value",
        sample_obj: "Ordinary Object",
        is_string: "Is it a string type",
        like_string_tip: "Such as strings, numbers, json objects, arrays, etc.;",
        like_string_tip2: "Note: If it is not a valid js object type when stored as an object type (such as illegal special characters, space effects), it may generate a report failure.",
        ele_pro: "Element Properties",
        like_ele_tip: "such as the element's name attribute, id attribute, value attribute, etc.",
        xpath_locator: "xpath path",
        xpath_tip: "Only supports element positioning in xpath mode, and returns a value",
        store: "Ordinary object (store)",
        store_text: "Element text (storeText)",
        store_value: "Element value (storeValue)",
        store_attr: "Element attribute (storeAttribute)",
        store_xpath_count: "Number of elements matching xpath (storeXpathCount)",
        store_window_handle: "Window Handle(storeWindowHandle)",
        store_title: "Web page title (storeTitle)",
        wait_time: "Wait time",
        per_tip: "After enabling the performance mode, the memory and cpu usage will be reduced",
        fail_over: "Failed to terminate",
        validate_tip: "Check means a hard assertion (assert), if the assertion fails, the program will terminate. Unchecked means a soft assertion (verify), if the assertion fails, the program will not terminate.",
        scenario: "Scenario",
        extract_type: "Please select the extraction information type",
        input_handle_name: "Please enter the storage window handle variable name",
        extract_tip: "Save the extracted content to a variable",
        extract_return_tip: "Save the extracted content to a variable, if the element does not exist, the return value is 'NoSuchElementException'",
        input_window_title: "Please enter the variable name to store the title of the webpage",
        revoke: "Revoke",
        is_required: "Required",
        remark: "Remark",
        result: "Result",
        var_step: "Variable generation steps",
        param_required_tip: "After debugging and saving, the usage of variables will be automatically verified. The parameters used in user-defined steps must be filled in; Not required is the redundant parameter not used in the custom step.",
        name: "Name",
        parameter_configuration: "Parameter configuration",
        enter_parameters: "Enter parameters",
        out_parameters: "Outer parameters",
        opt_ele: "Operation element",
        dst_ele: "Destination element",
        drag_type: "Drag and drop method",
        drag_end_point: "The final position of the dragged element",
        add_file: "Add file",
        file: "File: 【",
        been_deleted: "】 has been deleted! Please select the file again!",
        click_force: "Force click",
        click_tip_3: "Checked, the element is blocked and can be forced to click",
        pls_sel_loctype: "Please select a location type",
        pls_input_locator: "Please fill in the location",
        import_template: "Import Template",
        download_template: "Download Template",
        import_desc: "Import Description",
        el_import_tip_1: "1. If the imported ID already exists, update the element;",
        el_import_tip_2: "2. If the imported ID is empty or the ID does not exist, add an element;",
        el_import: "Element import",
        empty_text: "No data yet",
        confirm_del_el: "Confirm delete element",
        confirm_del: "Confirm delete",
        confirm_del_in: "Confirm delete command",
        deng: "wait",
        ge_instruction: "Instructions?",
        ge_el: "elements?",
        ge_scenario: "Scenarios?",
        view_ref: "View Reference",
        unplanned_element: "Unplanned element",
        scenario_instruction: "Scenario/Instruction",
        element_beening: "The element under the module is being",
        element_beening_desc: "element used be scenario",
        reference: "reference",
        continue_or_not: "Whether to continue",
        continue_or_not_delete: "Whether to continue delete",
        unplanned_scenario: "Unplanned Scenario",
        unplanned_module: "Unplanned Module",
        confrim_del_node: "OK to delete node",
        and_all_sub_node: "All resources under its subnodes?",
        instruction_is_referenced: "Instruction is referenced:",
        scenario_is_referenced: "Scenario is referenced:",
        confirm_del_ins: "Confirm Delete Instruction",
        confirm_del_scen: "Confirm delete scene",
        check_grid: "Connection failed, please check selenium-grid service status",
        check_grid_ip: "Connection failed, please check selenium-grid address information",
        view_config: "View configuration information",
        config_ip: "The local ip and port information are not detected, please check",
        personal_info: "Personal Information",
        in_config: "In Settings",
        scenario_steps_label: "Scenario Steps",
        command_steps_label: "Instruction Steps",
        assert_in_text: "Element text contains expectations (InText)",
        assert_not_in_text: "Element text does not contain expectations (NotInText)",
        equal: "equal",
        not_equal: "not equal to",
        contain: "contains",
        not_contain: "Does not contain",
        greater: "greater than",
        greater_equal: "greater than or equal to",
        lower: "less than",
        lower_equal: "Less than or equal to",
        null: "empty",
        not_null: "not null",
        assertion_configuration: "Assertion Configuration",
        smart_variable_enable: "The current scene variables are used first, if not, the original scene variables are used",
        use_origin_variable_scene: "Use original scene variables",
        use_origin_env_run: "Use original scene environment to execute",
        open_new: "Open new page",
        open_new_tip: "open in new page",
        no_scenario_edit_permission: "The current user does not have permission to modify",
        no_scenario_read_permission: "The current user does not have permission to view",
        no_scenario_create_permission: "The current user does not have permission to create",
        no_scenario_copy_permission: "The current user has no copy permission",
        automation: {
            scenario: {
                module_id: "Scenario Module ID",
                principal: "Principal",
                step_total: "Step total",
                schedule: "Schedule",
                last_result: "Last result",
                order: "Order",
                environment_type: "Environment type",
                environment_json: "Environment json",
                environment_group_id: "Environment group ID",
                id: "ID",
                name: "Name",
                level: "Level",
                status: "Status",
                delete_user: "Delete user",
            }
        }
    },
    filters: {
        schedule: {
            open: 'Open',
            close: 'Closed',
            unset: 'Unset',
        },
    },
    file_manage: {
        my_file: 'My File',
        update_user: 'Update User',
        all_file: 'All File',
        file_download: 'Download',
        batch_delete: 'Batch Delete',
        batch_move: 'Batch Move',
        batch_download: 'Batch Download',
        file_name_already_exists:"File name already exists",
        file_module:"File module",
        file_size_limit: "The file size does not exceed 50 M. Files that already exist in the project cannot be uploaded repeatedly",
        file_module_already_exists:"File already exists in this project",
        file_module_is_null:"Please select module",
        file_is_null:"Select at least one file"


    },
};

