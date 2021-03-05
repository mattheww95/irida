const path = require("path");
const webpack = require("webpack");
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const CssMinimizerPlugin = require("css-minimizer-webpack-plugin");
const TerserPlugin = require("terser-webpack-plugin");
const i18nThymeleafWebpackPlugin = require("./webpack/i18nThymeleafWebpackPlugin");
const entries = require("./entries");
const formatAntStyles = require("./styles");

const antColours = formatAntStyles();

module.exports = (env, argv) => {
  const isProduction = argv.mode === "production";

  return {
    devtool: isProduction ? "source-map" : "eval-source-map",
    cache: {
      type: "filesystem",
    },
    entry: entries,
    resolve: {
      extensions: [".js", ".jsx"],
      symlinks: false,
    },
    output: {
      filename: "js/[name].bundle.js",
      path: path.resolve(__dirname, "dist"),
      pathinfo: false,
    },
    externals: {
      jquery: "jQuery",
    },
    module: {
      rules: [
        {
          test: /\.(js|jsx)$/i,
          include: path.resolve(__dirname, "resources/js"),
          loader: "babel-loader",
          options: {
            cacheCompression: false,
            cacheDirectory: true,
          },
        },
        {
          test: /\.less$/i,
          use: [
            MiniCssExtractPlugin.loader,
            "css-loader",
            {
              loader: "less-loader",
              options: {
                lessOptions: {
                  modifyVars: antColours,
                  javascriptEnabled: true,
                },
              },
            },
          ],
        },
        {
          test: /\.css$/,
          use: [
            MiniCssExtractPlugin.loader,
            { loader: "css-loader", options: { importLoaders: 1 } },
            "postcss-loader",
          ],
        },
        {
          test: /\.(png|svg|jpg|jpeg|gif)$/i,
          type: "asset/resource",
        },
        {
          test: /\.(woff|woff2|eot|ttf|otf)$/i,
          type: "asset/resource",
        },
      ],
    },
    optimization: {
      ...(isProduction
        ? {
            minimize: true,
            minimizer: [
              new CssMinimizerPlugin({ parallel: true }),
              new TerserPlugin({ parallel: true, include: /\/resources/ }),
            ],
          }
        : { minimize: false }),
    },
    plugins: [
      new MiniCssExtractPlugin({
        ignoreOrder: true,
        filename: "css/[name].bundle.css",
      }),
      new i18nThymeleafWebpackPlugin({
        functionName: "i18n",
      }),
      new webpack.ProvidePlugin({
        i18n: path.resolve(path.join(__dirname, "resources/js/i18n")),
        process: "process/browser",
      }),
    ],
  };
};
