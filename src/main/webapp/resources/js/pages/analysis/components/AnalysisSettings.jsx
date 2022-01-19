/*
 * This file renders the details for the analysis as well as,
 * lazily loads the Samples, Share, and Delete components (component
 * is only loaded when the corresponding tab is clicked
 */

/*
 * The following import statements makes available all the elements
 *required by the components encompassed within
 */

import React, { Suspense, useContext } from "react";
import { Layout, Menu } from "antd";
import { Link, useLocation, Outlet } from "react-router-dom";

import { AnalysisContext } from "../../../contexts/AnalysisContext";
import { AnalysisDetailsContext } from "../../../contexts/AnalysisDetailsContext";

import { SPACE_MD } from "../../../styles/spacing";
import { ContentLoading } from "../../../components/loader/ContentLoading";
import { grey1 } from "../../../styles/colors";
import { ANALYSIS, SETTINGS } from "../routes";
import { setBaseUrl } from "../../../utilities/url-utilities";

const { Content, Sider } = Layout;

const pathRegx = /\/settings\/(?<path>\w+)/;

export default function AnalysisSettings() {
  const location = useLocation();
  const [current, setCurrent] = React.useState();
  const { analysisDetailsContext } = useContext(AnalysisDetailsContext);
  const { analysisContext, analysisIdentifier } = useContext(AnalysisContext);

  const DEFAULT_URL = setBaseUrl(
    `/analysis/${analysisIdentifier}/` + ANALYSIS.SETTINGS
  );

  const analysisRunning =
    !analysisContext.isError && !analysisContext.isCompleted;

  React.useEffect(() => {
    const found = location.pathname.match(pathRegx);
    const path = found ? found.groups.path : SETTINGS.DETAILS;
    setCurrent(path);
  }, [location.pathname]);

  /*
   * The following renders the analysis details, and tabs
   * for Samples, Share Results, and Delete Analysis which
   * the components are only loaded if the corresponding
   * tab is clicked
   */
  return (
    <Layout>
      <Sider width={200} style={{ backgroundColor: grey1 }}>
        <Menu mode="vertical" selectedKeys={[current]}>
          <Menu.Item key="details">
            <Link to={`${DEFAULT_URL}/${SETTINGS.DETAILS}`}>
              {i18n("AnalysisDetails.details")}
            </Link>
          </Menu.Item>
          <Menu.Item key="samples">
            <Link to={`${DEFAULT_URL}/${SETTINGS.SAMPLES}`}>
              {i18n("AnalysisSamples.samples")}
            </Link>
          </Menu.Item>
          {analysisDetailsContext.updatePermission
            ? [
                analysisContext.isError ? null : (
                  <Menu.Item key="share">
                    <Link to={`${DEFAULT_URL}/${SETTINGS.SHARE}`}>
                      {i18n("AnalysisShare.manageResults")}
                    </Link>
                  </Menu.Item>
                ),
                <Menu.Item key="delete">
                  <Link to={`${DEFAULT_URL}/${SETTINGS.DELETE}`}>
                    {i18n("AnalysisDelete.deleteAnalysis")}
                  </Link>
                </Menu.Item>,
              ]
            : null}
        </Menu>
      </Sider>

      <Layout style={{ paddingLeft: SPACE_MD, backgroundColor: grey1 }}>
        <Content>
          <Suspense fallback={<ContentLoading />}>
            <Outlet />
          </Suspense>
        </Content>
      </Layout>
    </Layout>
  );
}
