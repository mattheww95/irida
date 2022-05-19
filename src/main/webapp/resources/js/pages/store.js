import { configureStore } from "@reduxjs/toolkit";
import { passwordResetApi } from "../apis/passwordReset";

/*
Redux Store for forgot password and activate account pages.
For more information on redux stores see: https://redux.js.org/tutorials/fundamentals/part-4-store
 */
export default configureStore({
  reducer: {
    [passwordResetApi.reducerPath]: passwordResetApi.reducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware().concat(passwordResetApi.middleware),
});
