import { configureStore } from "@reduxjs/toolkit";
import { sampleApi } from "../../apis/samples/samples";
import sampleReducer from "./sampleSlice";
import sampleFilesReducer from "./sampleFilesSlice";

/*
Redux Store for sample details and metadata.
For more information on redux stores see: https://redux.js.org/tutorials/fundamentals/part-4-store
 */
export default configureStore({
  reducer: {
    sampleReducer,
    sampleFilesReducer,
    [sampleApi.reducerPath]: sampleApi.reducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware().concat(sampleApi.middleware),
});
