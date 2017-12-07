/**
 * Put all bundles to be created in this file.
 *  bundle_name ==> location_off_entry_file.
 *  Webpack will then create the bundle in `resource/js/build/`
 */
module.exports = {
  vendor: ["lodash", "babel-polyfill"],
  announcements: "./resources/js/pages/announcement/announcements.js",
  app: "./resources/js/app.js",
  dashboard: "./resources/js/pages/dashboard.js",
  "project-events": "./resources/js/pages/projects/project-events.js",
  projects: "./resources/js/pages/projects/projects.js",
  "samples-metadata-import":
    "./resources/js/pages/projects/samples-metadata-import/index.js",
  "project-samples": "./resources/js/pages/projects/samples/project-samples.js",
  "project-samples-merge":
    "./resources/js/pages/projects/samples/modals/samples-merge.js",
  "project-samples-copy":
    "./resources/js/pages/projects/samples/modals/samples-copy.js",
  "project-samples-remove":
    "./resources/js/pages/projects/samples/modals/samples-remove.js",
  "project-samples-filter":
    "./resources/js/pages/projects/samples/modals/samples-filter.js",
  "project-samples-linker":
    "./resources/js/pages/projects/samples/modals/samples-linker.js",
  "project-add-sample": "./resources/js/pages/projects/project-add-samples.js",
  "project-linelist": "./resources/js/pages/projects/linelist/index.js",
  "create-metadata-template":
    "./resources/js/pages/projects/metadata-template/create-metadata-template.js",
  "visualizations-phylogenetics":
    "./resources/js/pages/visualizations/phylogenetics/index.js",
  "projects-associated-edit":
    "./resources/js/pages/projects/associated-projects/edit.module.js",
  "project-users-groups":
    "./resources/js/pages/projects/project-users-groups.js",
  "sample-files": "./resources/js/pages/samples/sample-files.js",
  "project-reference-files":
    "./resources/js/pages/projects/project-reference-files.js",
  "analyses-table": "./resources/js/pages/analyses/analyses-table.js",
  "clients-list": "./resources/js/pages/clients/clients-list.js",
  "users-list": "./resources/js/pages/users/users-list.js",
  "users-password": "./resources/js/pages/users/users-password.js",
  "announcement-users":
    "./resources/js/pages/announcement/announcement-users.js",
  "announcement-admin":
    "./resources/js/pages/announcement/announcement-admin.js",
  "sequencing-runs":
    "./resources/js/pages/sequencing-runs/sequencing-runs-list.js",
  "groups-list": "./resources/js/pages/users/groups-list.js",
  "group-members": "./resources/js/pages/users/groups-members.js",
  "ncbi-exports": "./resources/js/pages/projects/ncbi-export.js",
  search: "./resources/js/pages/search/search.js"
};
