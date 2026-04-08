import type {
	HybridView,
	HybridViewMethods,
	HybridViewProps,
} from "react-native-nitro-modules";
import type { OmniViewProps } from "../types/view";

export interface Props extends HybridViewProps, OmniViewProps {}

export type OmniView = HybridView<
	Props,
	HybridViewMethods,
	{ android: "kotlin" }
>;
