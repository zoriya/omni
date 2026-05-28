const { getDefaultConfig, mergeConfig } = require("@react-native/metro-config");
const path = require("node:path");
const root = path.resolve(__dirname, "..");

const useWeb = process.env.USE_WEB === "true" || process.env.PLATFORM === "web";

/**
 * Metro configuration
 * https://facebook.github.io/metro/docs/configuration
 *
 * @type {import('metro-config').MetroConfig}
 */
const config = {
  watchFolders: [root],
  ...(useWeb && {
    resolver: {
      extraNodeModules: {
        "react-native": path.resolve(__dirname, "node_modules", "react-native-web"),
      },
    },
  }),
  transformer: {
    getTransformOptions: async () => ({
      transform: {
        experimentalImportSupport: false,
        inlineRequires: true,
      },
    }),
  },
};

module.exports = mergeConfig(getDefaultConfig(__dirname), config);
