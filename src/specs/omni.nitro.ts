import type {
	HybridView,
	HybridViewMethods,
	HybridViewProps,
} from "react-native-nitro-modules";

export interface OmniProps extends HybridViewProps {
	isRed: boolean;
}

export interface OmniMethods extends HybridViewMethods {}

export type Omni = HybridView<OmniProps, OmniMethods, { android: "kotlin" }>;
