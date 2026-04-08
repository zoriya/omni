import type { HybridObject } from "react-native-nitro-modules";
import type { OmniPlayer as OmniPlayerT } from "../types/player";
import type { OmniPlayerProps as OmniPlayerPropsT } from "../types/provider";

export interface OmniPlayerProps
	extends HybridObject<{ android: "kotlin" }>,
		OmniPlayerPropsT {}

export interface OmniPlayer
	extends HybridObject<{ android: "kotlin" }>,
		OmniPlayerT {}

export interface OmniPlayerFactory extends HybridObject<{ android: "kotlin" }> {
	createPlayer(props: OmniPlayerProps): OmniPlayer;
}
