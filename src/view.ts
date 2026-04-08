import { getHostComponent, type HybridRef, type HybridViewMethods } from "react-native-nitro-modules";
import OmniConfig from "../nitrogen/generated/shared/json/OmniViewConfig.json";
import type { Props } from "./specs/omni-view.nitro";

export const OmniView = getHostComponent<Props, HybridViewMethods>(
	"OmniView",
	() => OmniConfig,
);

export type OmnViewiRef = HybridRef<Props, HybridViewMethods>;
