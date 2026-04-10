import type { HybridObject } from "react-native-nitro-modules";
import type { OmniPlayer as OmniPlayerT } from "../types/player";
import type { Source } from "../types/source";

export interface OmniPlayer
	extends HybridObject<{ android: "kotlin" }>,
		OmniPlayerT {}

export interface OmniPlayerFactory extends HybridObject<{ android: "kotlin" }> {
	createPlayer(props: Source): OmniPlayer;
}
