import React, { useMemo } from "react";
import { formatInternationalizedDateTime } from "../../utilities/date-utilities";
import { getPaginationOptions } from "../../utilities/antdesign-table-utilities";
import { Button, Table, TableProps } from "antd";
import { SearchProject, SearchSample } from "./SearchLayout";
import ProjectTag from "./ProjectTag";
import { ColumnsType } from "antd/lib/table";
import { SampleDetailViewer } from "../../components/samples/SampleDetailViewer";

type SearchSamplesTableParams = {
  samples:
    | {
        content: SearchSample[];
        total: number;
      }
    | undefined;
  handleTableChange: TableProps<SearchSample>["onChange"];
};

/**
 * React component to render a table to display samples found in global search
 * @param samples
 * @param handleTableChange
 * @constructor
 */
export default function SearchSamplesTable({
  samples,
  handleTableChange,
}: SearchSamplesTableParams) {
  const columns = useMemo<ColumnsType<SearchSample>>(
    () => [
      {
        key: `sampleName`,
        dataIndex: "name",
        title: "NAME",
        sorter: true,
        render: (name, sample) => {
          return (
            <SampleDetailViewer
              sampleId={sample.id}
              projectId={sample.projects[0].id}
            >
              <Button>{name}</Button>
            </SampleDetailViewer>
          );
        },
      },
      {
        key: `organism`,
        dataIndex: `organism`,
        title: "ORGANISM",
        sorter: true,
      },
      {
        key: `projects`,
        dataIndex: `projects`,
        title: `PROJECTS`,
        render: (projects: SearchProject[]) => {
          return projects.map((project) => (
            <ProjectTag key={`pTag-${project.id}`} project={project} />
          ));
        },
      },
      {
        key: `createdDate`,
        dataIndex: `createdDate`,
        title: `CREATED DATE`,
        render: (text: string) => formatInternationalizedDateTime(text),
        sorter: true,
        width: 200,
      },
      {
        key: `modifiedDate`,
        dataIndex: `modifiedDate`,
        title: `MODIFIED DATE`,
        render: (text: string) => formatInternationalizedDateTime(text),
        sorter: true,
        defaultSortOrder: "descend",
        width: 200,
      },
    ],
    []
  );

  return (
    <Table
      dataSource={samples?.content}
      columns={columns}
      pagination={
        samples?.total ? getPaginationOptions(samples.total) : undefined
      }
      onChange={handleTableChange}
    />
  );
}
