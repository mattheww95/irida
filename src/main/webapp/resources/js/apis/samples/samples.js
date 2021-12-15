import axios from "axios";
import { setBaseUrl } from "../../utilities/url-utilities";
import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";

const URL = setBaseUrl(`ajax/samples`);

const SEQUENCE_FILES_URL = setBaseUrl("/sequenceFiles/");

/**
 * Redux API to handle queries based on samples
 */
export const sampleApi = createApi({
  reducerPath: `sampleApi`,
  baseQuery: fetchBaseQuery({
    baseUrl: setBaseUrl(URL),
  }),
  tagTypes: ["SampleDetails"],
  endpoints: (build) => ({
    /*
    Get the default information about a sample
     */
    getSampleDetails: build.query({
      query: (sampleId) => ({
        url: `/${sampleId}/details`,
      }),
      providesTags: ["SampleDetails"],
    }),
    /*
    Update sample details
     */
    updateSampleDetails: build.mutation({
      query: ({ sampleId, field, value }) => ({
        url: `/${sampleId}/details`,
        body: { field, value },
        method: "PUT",
      }),
      invalidatesTags: ["SampleDetails"],
    }),
    addSampleMetadata: build.mutation({
      query: ({
        sampleId,
        projectId,
        metadataField,
        metadataEntry,
        metadataRestriction,
      }) => ({
        url: `/${sampleId}/metadata`,
        body: { projectId, metadataField, metadataEntry, metadataRestriction },
        method: "POST",
      }),
      invalidatesTags: ["SampleMetadata"],
    }),
    removeSampleMetadata: build.mutation({
      query: ({ field, entryId, projectId }) => ({
        url: `/metadata?projectId=${projectId}&metadataField=${field}&metadataEntryId=${entryId}`,
        method: "DELETE",
      }),
      invalidatesTags: ["SampleMetadata"],
    }),
    updateSampleMetadata: build.mutation({
      query: ({
        sampleId,
        projectId,
        metadataFieldId,
        metadataField,
        metadataEntryId,
        metadataEntry,
        metadataRestriction,
      }) => ({
        url: `/${sampleId}/metadata`,
        body: {
          projectId,
          metadataFieldId,
          metadataField,
          metadataEntryId,
          metadataEntry,
          metadataRestriction,
        },
        method: "PUT",
      }),
      invalidatesTags: ["SampleMetadata"],
    }),
    removeSampleFiles: build.mutation({
      query: ({ sampleId, fileObjectId, type }) => ({
        url: `/${sampleId}/files?fileObjectId=${fileObjectId}&fileType=${type}`,
        method: "DELETE",
      }),
      invalidatesTags: ["SampleFiles"],
    }),
  }),
});

export const {
  useGetSampleDetailsQuery,
  useUpdateSampleDetailsMutation,
  useAddSampleMetadataMutation,
  useRemoveSampleMetadataMutation,
  useUpdateSampleMetadataMutation,
  useRemoveSampleFilesMutation,
} = sampleApi;

/**
 * Gets the sample metadata.
 * @param {Object} params
 * @returns {Promise<{}|T>}
 */
export const fetchMetadataForSample = async ({ sampleId, projectId }) => {
  try {
    const { data } = await axios.get(
      setBaseUrl(`${URL}/${sampleId}/metadata?projectId=${projectId}`)
    );
    return data;
  } catch (e) {
    return Promise.reject();
  }
};

/**
 * Get file details for a sample
 * @param {number} sampleId - identifier for a sample
 * @param {number} projectId - identifier for a project (if the sample is in the cart), not required.
 * @returns {Promise<any>}
 */
export async function fetchSampleFiles({ sampleId, projectId }) {
  try {
    const response = await axios(
      `${URL}/${sampleId}/files${projectId && `?projectId=${projectId}`}`
    );
    return response.data;
  } catch (e) {
    return Promise.reject(e.response.data);
  }
}

/**
 * Download genome assembly file
 * @param {number} sampleId - identifier for a sample
 * @param {number} genomeAssemblyId - identifier for the genomeassembly to download
 * @returns {Promise<any>}
 */
export function downloadGenomeAssemblyFile({ sampleId, genomeAssemblyId }) {
  window.open(
    `${URL}/${sampleId}/assembly/download?genomeAssemblyId=${genomeAssemblyId}`,
    "_blank"
  );
}

/**
 * Download a sequence file
 * @param {number} sequencingObjectId - identifier for the sequencingobject
 * @param {number} sequenceFileId - identifier for the sequence file to download
 * @returns {Promise<any>}
 */
export function downloadSequencingObjectFile({
  sequencingObjectId,
  sequenceFileId,
}) {
  window.open(
    `${SEQUENCE_FILES_URL}/download/${sequencingObjectId}/file/${sequenceFileId}`,
    "_blank"
  );
}
