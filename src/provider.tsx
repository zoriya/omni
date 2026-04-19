import { createContext, type ReactNode, useContext, useEffect } from "react";
import { NitroModules } from "react-native-nitro-modules";
import type { OmniPlayerFactory } from "./specs/omni-player.nitro";
import type { OmniPlayer } from "./types/player";
import type { Source } from "./types/source";
import { useLazyRef } from "./utils/lazy-ref";

const ProviderFactory = NitroModules.createHybridObject<OmniPlayerFactory>(
	"OmniPlayerFactory",
);

const PlayerCtx = createContext<OmniPlayer>(null!);

export const OmniProvider = ({
	children,
	source,
}: {
	source: Source;
	children: ReactNode;
}) => {
	const player = useLazyRef(() => ProviderFactory.createPlayer(source));

	useEffect(() => {
		player.current.source = source;
	}, [source]);

	return (
		<PlayerCtx.Provider value={player.current}>{children}</PlayerCtx.Provider>
	);
};

export const usePlayer = () => {
	return useContext(PlayerCtx);
};
