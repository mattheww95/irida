import { List, fromJS } from "immutable";

export const NO_TEMPLATE_INDEX = 0;

export const types = {
  LOAD: "METADATA/TEMPLATES/LOAD_REQUEST",
  LOAD_ERROR: "METADATA/TEMPLATES/LOAD_TEMPLATES_ERROR",
  LOAD_SUCCESS: "METADATA/TEMPLATES/LOAD_TEMPLATES_SUCCESS",
  USE_TEMPLATE: "METADATA/TEMPLATES/USE_TEMPLATE",
  TABLE_MODIFIED: "METADATA/TEMPLATES/TABLE_MODIFIED",
  SAVE_TEMPLATE: "METADATA/TEMPLATES/SAVE_TEMPLATE",
  SAVING_TEMPLATE: "METADATA/TEMPLATES/SAVING_TEMPLATE",
  SAVED_TEMPLATE: "METADATA/TEMPLATES/SAVED_TEMPLATE",
  SAVE_COMPLETE: "METADATA/TEMPLATES/SAVE_COMPLETE",
  TEMPLATE_MODIFIED: "METADATA/TEMPLATES/TEMPLATE_MODIFIED"
};

const initialState = fromJS({
  fetching: false,
  error: false,
  templates: List(),
  current: NO_TEMPLATE_INDEX,
  saving: false,
  saved: false
});

export const reducer = (state = initialState, action = {}) => {
  switch (action.type) {
    case types.LOAD:
      return state.set("fetching", true).set("error", false);
    case types.LOAD_SUCCESS:
      const templates = action.templates.map(t => {
        t.modified = [];
        return t;
      });
      return state.set("fetching", false).set("templates", fromJS(templates));
    case types.LOAD_ERROR:
      return state.set("fetching", false).set("error", true);
    case types.USE_TEMPLATE:
      /*
      Update teh current template to show unmodified
      If the action.index == the current template the it will just
      reset the modified state.
       */
      return state
        .setIn(["templates", state.get("current"), "modified"], [])
        .set("current", action.index);
    case types.TABLE_MODIFIED:
      return state.setIn(
        ["templates", state.get("current"), "modified"],
        action.fields
      );
    case types.SAVE_TEMPLATE:
      return state.set("saving", true);
    case types.SAVED_TEMPLATE:
      let template = action.template;
      let index = state
        .get("templates")
        .findIndex(t => t.get("id") === template.id);
      if (index > 0) {
        state = state.setIn(["templates", index], fromJS(template));
      } else {
        state = state.update("templates", templates =>
          templates.push(fromJS(template))
        );
        index = state.get("templates").size - 1;
      }
      return state
        .set("saving", false)
        .set("saved", true)
        .set("modified", fromJS([]))
        .set("current", index);
    case types.SAVE_COMPLETE:
      return state.set("saved", false);
    case types.TEMPLATE_MODIFIED:
      return state.setIn(
        ["templates", state.get("current"), "modified"],
        action.fields
      );
    default:
      return state;
  }
};

export const actions = {
  load: () => ({ type: types.LOAD }),
  success: templates => ({ type: types.LOAD_SUCCESS, templates }),
  error: error => ({ type: types.LOAD_ERROR, error }),
  use: index => ({ type: types.USE_TEMPLATE, index }),
  tableModified: fields => ({ type: types.TABLE_MODIFIED, fields }),
  saveTemplate: (name, fields, id) => ({
    type: types.SAVE_TEMPLATE,
    data: { name, fields, id }
  }),
  savedTemplate: template => ({ type: types.SAVED_TEMPLATE, template }),
  savedComplete: () => ({ type: types.SAVE_COMPLETE }),
  templateModified: fields => ({ type: types.TEMPLATE_MODIFIED, fields })
};
