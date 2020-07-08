/* eslint-disable */
'use strict';

import React, {useState, useEffect, forwardRef, useImperativeHandle} from "react";
import {Check} from "components/input/Check";
import {Form} from "components/input/Form";
import {Text} from "components/input/Text";
import {Radio} from "components/input/Radio";
import {Button} from "components/buttons";
import {Combobox} from "components/combobox";

const MaintenanceScheduleEdit = forwardRef((props, ref) => {
    const [model, setModel] = useState({
        scheduleName: "",
        scheduleType: "SINGLE",
        calendarName: "",
        strategy: false
    });
    const [calendarAdded, setCalendarAdded] = useState(false);
    const [selectedCalendar, setSelectedCalendar] = useState(0);

    useEffect(() => {
        if(props.isEdit) {
            setModel({...model,
                scheduleName: props.schedule.scheduleName,
                scheduleType: props.schedule.scheduleType,
                calendarName: props.schedule.calendarName
            });
            setCalendarAdded(!!props.schedule.calendarName);
            setSelectedCalendar(
                (props.calendarNames.length > 1 && props.schedule.calendarName) ?
                    props.calendarNames.filter(name => name.text === props.schedule.calendarName)[0].id : 0
            );
        }
    }, [props.schedule]);

    const onFormChanged = (newModel) => {
        /* strategy gets initialized as empty string, but we want the initial value to be false.
         * Is equivalent to: if strategy is "" then set it to false */
        newModel.strategy === "" && (newModel.strategy = false);
        setModel({
            scheduleName: newModel.scheduleName,
            scheduleType: newModel.scheduleType,
            calendarName: newModel.calendarName,
            strategy: newModel.strategy
        });
    };

    const onSelectCalendar = (item) => {
        setSelectedCalendar(item.id);
        setModel({...model, calendarName: item.text === "<None>" ? "" : item.text});
    };

    useImperativeHandle(ref, () => ({
        onEdit() {
            const params = {
                scheduleName: model.scheduleName,
                scheduleType: model.scheduleType,
                // Ignore selected name if selection dropdown is closed
                calendarName: calendarAdded ? model.calendarName : "",
            };
            if (props.isEdit) {
                params.scheduleId = props.schedule.scheduleId;
                params.strategy = model.strategy ? "Cancel" : "Fail";
            }
            props.onEdit(params);
        }
    }));

    return (
        <Form onChange={onFormChanged} model={model}>
            <Text name="scheduleName" required type="text" label={t("Schedule Name")}
                  labelClass="col-sm-3" divClass="col-sm-6"
                  disabled={props.isEdit} />
            <Radio defaultValue="SINGLE" name="scheduleType" inline={true} label={t('Type')} labelClass="col-md-3" divClass="col-md-6"
                   items={[
                       {label: <b>{t('Single')}</b>, value: 'SINGLE'},
                       {label: <b>{t('Multi')}</b>, value: 'MULTI'},
                   ]}
            />
            {props.isEdit &&
            <Check name="strategy" label={<b>{t("Cancel affected actions")}</b>} divClass="col-md-6 col-md-offset-3" />
            }
            <div className="form-group">
                <div className="col-md-6 col-md-offset-3">
                    {!calendarAdded ?
                        <Button className="btn-default" text={t("Add Calendar")} icon="fa-chevron-down "
                                handler={() => setCalendarAdded(!calendarAdded)} />
                        :
                        <div className="panel panel-default">
                            <div className="panel-heading no-padding">
                                <Button text={t("Add Calendar")} icon="fa-chevron-up"
                                        handler={() => setCalendarAdded(!calendarAdded)} />
                            </div>
                            <div className="panel-body">
                                <div className="form-horizontal">
                                    <div className="form-group">
                                        <label className="col-md-3 control-label">{t("Calendar")}:</label>
                                        <div className="col-md-7">
                                            <Combobox id="calendarSelect" name="calendarSelect"
                                                      data={props.calendarNames}
                                                      selectedId={selectedCalendar}
                                                      onSelect={onSelectCalendar}
                                            />
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    }
                </div>
            </div>
        </Form>
    );
});

export default MaintenanceScheduleEdit;
