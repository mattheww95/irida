import axios from "axios";
import { setBaseUrl } from "../../utilities/url-utilities";
import type { CurrentUser } from "../../types/irida";
/**
 * @file API methods for the currently logged in user
 */

/**
 * Get details about the currently logged in user
 * @returns {Promise<unknown>}
 */
export async function fetchCurrentUserDetails(): Promise<CurrentUser> {
  try {
    const { data } = await axios.get(setBaseUrl(`/ajax/users/current`));
    return Promise.resolve(data);
  } catch (e) {
    return Promise.reject(e.response.data);
  }
}
