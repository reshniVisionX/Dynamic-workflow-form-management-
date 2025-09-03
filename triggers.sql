
CREATE TRIGGER workflow_logic_trigger
AFTER INSERT OR UPDATE ON workflow_trigger
FOR EACH ROW
EXECUTE FUNCTION workflow_logic_trigger_function();


CREATE OR REPLACE FUNCTION get_workflow_table_name(p_wkfl_id INT)
RETURNS TEXT AS $$
DECLARE
    v_workflow_table_name TEXT;
BEGIN
    SELECT wrkfl_name INTO v_workflow_table_name
    FROM workflow
    WHERE wkfl_id = p_wkfl_id;
    
    RETURN lower(v_workflow_table_name);
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION update_workflow_table_status(
    p_workflow_table_name TEXT, 
    p_status TEXT, 
    p_cur_id INT
)
RETURNS VOID AS $$
BEGIN
    EXECUTE format('UPDATE %I SET status = $1 WHERE cur_id = $2', p_workflow_table_name)
    USING p_status, p_cur_id;
    
    RAISE NOTICE 'Updated workflow table: % with status %', p_workflow_table_name, p_status;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION delete_workflow_entries(p_cur_id INT)
RETURNS VOID AS $$
BEGIN
    DELETE FROM workflow_action WHERE cur_id = p_cur_id;
    DELETE FROM workflow_trigger WHERE cur_id = p_cur_id;
    
    RAISE NOTICE 'Deleted workflow entries for cur_id: %', p_cur_id;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION notification_upd(message TEXT, approver_user_id INT)
RETURNS VOID AS $$
BEGIN
    
    IF message IS NULL OR message = '' THEN
        RAISE EXCEPTION 'Notification message cannot be null or empty.';
    END IF;

    IF approver_user_id IS NULL THEN
        RAISE EXCEPTION 'Approver user ID cannot be null.';
    END IF;

    INSERT INTO notification (user_id, message)
    VALUES (approver_user_id, message);

    RAISE NOTICE 'Notification sent: % to user_id: %', message, approver_user_id;
END;
$$ LANGUAGE plpgsql;







CREATE OR REPLACE FUNCTION workflow_logic_trigger_function()
RETURNS TRIGGER AS $$
DECLARE
    current_json JSONB;              
    current_block JSONB;              
    next_state TEXT;                  
    workflow_status TEXT;             
    approver_user_id INT;             
    notification_message TEXT;       
    workflow_table_name TEXT;         
    wrkfl_id INT;                     
BEGIN
    RAISE NOTICE 'Function triggered for cur_id: %, updates: %', NEW.cur_id, NEW.updates;

    BEGIN
       
        SELECT wa.state, wa.current_state, wa.user_id, wa.wkfl_id
        INTO current_json, workflow_status, approver_user_id, wrkfl_id
        FROM workflow_action wa
        WHERE wa.cur_id = NEW.cur_id;

        RAISE NOTICE 'Fetched workflow action: state = %, current_state = %, user_id = %, wkfl_id = %',
            current_json, workflow_status, approver_user_id, wrkfl_id;

      
        IF current_json IS NULL THEN
            RAISE EXCEPTION 'No workflow action found for cur_id: %', NEW.cur_id;
        END IF;

        
        current_block := (
            SELECT json_block 
            FROM jsonb_array_elements(current_json) AS json_block
            WHERE json_block ->> 'state' = workflow_status
        );

        RAISE NOTICE 'Fetched current block: %', current_block;

        IF current_block IS NULL THEN
            RAISE EXCEPTION 'No matching block found for current_state: %', workflow_status;
        END IF;

        RAISE NOTICE 'Processing updates type: %', NEW.updates;

        CASE NEW.updates
            WHEN 'approved' THEN
                RAISE NOTICE 'Approved path initiated for cur_id: %', NEW.cur_id;

                next_state := current_block ->> 'approved';
                RAISE NOTICE 'Next state determined: %', next_state;

                IF next_state IS NULL THEN
                    RAISE EXCEPTION 'No "approved" field found in current block: %', current_block;
                END IF;

                IF next_state = 'success' THEN
                    RAISE NOTICE 'Processing success terminal state for cur_id: %', NEW.cur_id;

                    workflow_table_name := get_workflow_table_name(wrkfl_id);

                    PERFORM update_workflow_table_status(
                        workflow_table_name, 
                        'completed', 
                        NEW.cur_id
                    );

                    notification_message := 'The workflow process has been successfully completed.';
                    PERFORM notification_upd(
                        notification_message, 
                        approver_user_id
                    );

                    PERFORM delete_workflow_entries(NEW.cur_id);

                    RAISE NOTICE '---End of success state for cur_id: %---', NEW.cur_id;
                    RETURN NEW;
                END IF;

                LOOP
                    
                    IF next_state = 'success' THEN
                        RAISE NOTICE 'Detected success state in loop for cur_id: %', NEW.cur_id;

                        SELECT wrkfl_name INTO workflow_table_name
                        FROM workflow
                        WHERE wkfl_id = wrkfl_id;

                        EXECUTE format('UPDATE %I SET status = $1 WHERE cur_id = $2', lower(workflow_table_name))
                        USING 'completed', NEW.cur_id;

                        notification_message := 'Workflow process has been successfully completed.';
                        PERFORM notification_upd(notification_message, approver_user_id);

                        DELETE FROM workflow_action WHERE cur_id = NEW.cur_id;
                        DELETE FROM workflow_trigger WHERE cur_id = NEW.cur_id;

                        RAISE NOTICE '---End of success loop for cur_id: %---', NEW.cur_id;
                        EXIT;
                    END IF;
					
                    RAISE NOTICE 'Fetching next block for state: %', next_state;
                    current_block := (
                        SELECT json_block
                        FROM jsonb_array_elements(current_json) AS json_block
                        WHERE json_block ->> 'state' = next_state
                    );

                    IF current_block IS NULL THEN
                        RAISE EXCEPTION 'No matching block found for next_state: %', next_state;
                    END IF;

                    IF current_block ->> 'status' = 'success' THEN
					approver_user_id := (
                    SELECT (json_block ->> 'approver_id')::INT
                    FROM jsonb_array_elements(current_json) AS json_block
                    WHERE json_block ->> 'state' = 'EmployeeUpdate'
                     );
					    notification_message := 'Your form is auto-approved by : ' || next_state;
                        PERFORM notification_upd(
                            notification_message, 
                            approver_user_id
                        );
                        next_state := current_block ->> 'approved';
						
						
                        CONTINUE;
                    ELSIF current_block ->> 'status' = 'pending' THEN
                        approver_user_id := (current_block ->> 'approver_id')::INT;
                        RAISE NOTICE 'Pending status detected. Approver ID: %', approver_user_id;

                        UPDATE workflow_action
                        SET 
                            current_state = next_state,
                            status = 'pending',
                            approver_id = approver_user_id,
                            state = current_json
                        WHERE cur_id = NEW.cur_id;

                        notification_message := 'Your action is required for the state: ' || next_state;
                        PERFORM notification_upd(
                            notification_message, 
                            approver_user_id
                        );

                        RAISE NOTICE '---Pending status updated for cur_id: %---', NEW.cur_id;
                        RETURN NEW;
                    ELSE
                        RAISE EXCEPTION 'Invalid status for state: %', next_state;
                    END IF;
                END LOOP;

            WHEN 'rejected' THEN
                RAISE NOTICE 'Rejected path initiated for cur_id: %', NEW.cur_id;

                next_state := current_block ->> 'rejected';
                RAISE NOTICE 'Next state for rejection determined: %', next_state;

                approver_user_id := (
                    SELECT (json_block ->> 'approver_id')::INT
                    FROM jsonb_array_elements(current_json) AS json_block
                    WHERE json_block ->> 'state' = 'EmployeeUpdate'
                );

                notification_message := 'Your workflow process has been rejected.';
                PERFORM notification_upd(
                    notification_message, 
                    approver_user_id
                );

                workflow_status := 'failed';
                RAISE NOTICE 'Updating workflow status to failed for cur_id: %', NEW.cur_id;

                workflow_table_name := get_workflow_table_name(wrkfl_id);

                PERFORM update_workflow_table_status(
                    workflow_table_name, 
                    'rejected', 
                    NEW.cur_id
                );

                PERFORM delete_workflow_entries(NEW.cur_id);

                RAISE NOTICE '---End of rejection process for cur_id: %---', NEW.cur_id;

            WHEN 'verification' THEN
    RAISE NOTICE 'Verification path initiated for cur_id: %', NEW.cur_id;

    
    next_state := current_block ->> 'verification';

    IF next_state IS NULL THEN
        RAISE NOTICE 'Verification state does not exist, falling back to rejection for cur_id: %', NEW.cur_id;

      
        next_state := current_block ->> 'rejected';
        RAISE NOTICE 'Next state for rejection determined: %', next_state;

        approver_user_id := (
            SELECT (json_block ->> 'approver_id')::INT
            FROM jsonb_array_elements(current_json) AS json_block
            WHERE json_block ->> 'state' = 'EmployeeUpdate'
        );

        notification_message := 'Your workflow process has been rejected due to improper data try resubmitting.';
        PERFORM notification_upd(
            notification_message, 
            approver_user_id
        );

        workflow_status := 'failed';
        RAISE NOTICE 'Updating workflow status to failed for cur_id: %', NEW.cur_id;

        workflow_table_name := get_workflow_table_name(wrkfl_id);

        PERFORM update_workflow_table_status(
            workflow_table_name, 
            'rejected', 
            NEW.cur_id
        );

        PERFORM delete_workflow_entries(NEW.cur_id);

        RAISE NOTICE '---End of rejection process for cur_id: %---', NEW.cur_id;

        RETURN NEW; 
    END IF;

    RAISE NOTICE 'Next state for verification determined: %', next_state;

    approver_user_id := (
        SELECT (json_block ->> 'approver_id')::INT
        FROM jsonb_array_elements(current_json) AS json_block
        WHERE json_block ->> 'state' = next_state
    );

   
    notification_message := 'You are requested to verify the data sent.';
    PERFORM notification_upd(
        notification_message, 
        approver_user_id
    );

    
    workflow_table_name := get_workflow_table_name(wrkfl_id);

    PERFORM update_workflow_table_status(
        workflow_table_name, 
        'verification', 
        NEW.cur_id
    );

    UPDATE workflow_action
    SET 
        status = 'verifying'
    WHERE cur_id = NEW.cur_id;

    RAISE NOTICE 'Verification notification sent to approver ID: %', approver_user_id;

       ELSE
                RAISE EXCEPTION 'Unexpected or invalid updates type: %', NEW.updates;
        END CASE;

        RAISE NOTICE '---End of function execution for cur_id: %---', NEW.cur_id;
        RETURN NEW;

    END;
END;
$$ LANGUAGE plpgsql;