import type { ConfigPlugin } from "@expo/config-plugins";
import { createRunOncePlugin } from "@expo/config-plugins";
import pkg from "../../package.json";
import { withMediaNotifications } from "./withMediaNotifications";
import { withPip } from "./withPip";

const withOmni: ConfigPlugin = (config) => {
	let nextConfig = withPip(config);
	nextConfig = withMediaNotifications(nextConfig);
	return nextConfig;
};

export default createRunOncePlugin(withOmni, pkg.name, pkg.version);

export { withMediaNotifications, withPip };
