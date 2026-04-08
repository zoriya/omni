import { createContext, type ReactNode, useContext } from "react";
import { NitroModules } from "react-native-nitro-modules";
import type {
	OmniPlayerFactory,
	OmniPlayerProps,
} from "./specs/omni-player.nitro";
import type { OmniPlayer } from "./types/player";

const ProviderFactory = NitroModules.createHybridObject<OmniPlayerFactory>(
	"OmniProviderFactory",
);

const PlayerCtx = createContext<OmniPlayer>(null!);

export const OmniProvider = ({
	children,
	...props
}: OmniPlayerProps & { children: ReactNode }) => {
	const player = ProviderFactory.createPlayer(props);
	return <PlayerCtx.Provider value={player}>{children}</PlayerCtx.Provider>;
};

export const usePlayer = () => {
	return useContext(PlayerCtx);
};
