import { setBaseUrl } from "../../utilities/url-utilities";
import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";

const BASE_URL = setBaseUrl(`ajax/sequencing-runs`);

/**
 * Redux API for sequencing runs.
 */
export const sequencingRunsApi = createApi({
  reducerPath: `sequencingRunsApi`,
  baseQuery: fetchBaseQuery({
    baseUrl: BASE_URL,
  }),
  tagTypes: ["Run"],
  endpoints: (build) => ({
    /*
    Get a sequencing run.
    */
    getSequencingRunDetails: build.query({
      query: (runId) => ({
        url: `${runId}/details`,
      }),
      providesTags: ["Run"],
    }),
    /*
    Get sequencing run files.
    */
    getSequencingRunFiles: build.query({
      query: (runId) => ({
        url: `${runId}/sequenceFiles`,
      }),
    }),
    /*
    Delete a sequencing run.
    */
    deleteSequencingRun: build.mutation({
      query: ({ runId }) => ({
        url: `${runId}`,
        method: "DELETE",
      }),
      invalidatesTags: ["Run"],
    }),
  }),
});

export const {
  useGetSequencingRunDetailsQuery,
  useGetSequencingRunFilesQuery,
  useDeleteSequencingRunMutation,
} = sequencingRunsApi;
