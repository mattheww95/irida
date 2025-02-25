// Import css
import "../css/app.css";
/*
This will load notifications into the global namespace.  Remove this once all
files have been converted over to wekbpack builds.
 */
import "./modules/notifications";
// Galaxy Alert if in galaxy session
import "./components/Header/PageHeader";
import { setBaseUrl } from "./utilities/url-utilities";

/*
Since IRIDA can be run on a servlet path, we need to make sure that all requests
get the correct base url.
 */
const xmlHttpRequestOpen = window.XMLHttpRequest.prototype.open;

function openBaseUrlModifier(method, url, async) {
  const newUrl = setBaseUrl(url);
  /*
  Call the original open method with the new url.
   */
  return xmlHttpRequestOpen.apply(this, [method, newUrl, async]);
}

window.XMLHttpRequest.prototype.open = openBaseUrlModifier;
