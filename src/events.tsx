import { useEffect, useState } from "react";
import { usePlayer } from "./provider";
import type { OmniPlayer } from "./specs/omni-player.nitro";
import type { OmniEvents } from "./types/events";
import type { OmniPlayerState } from "./types/player";

function capitalize<T extends string>(str: T): Capitalize<T> {
	return (str.charAt(0).toUpperCase() +
		str.slice(1).toLowerCase()) as Capitalize<T>;
}

export const useEvent = <Event extends keyof OmniEvents>(
	event: Event,
	callback: OmniEvents[Event],
) => {
	const player = usePlayer() as OmniPlayer;
	useEffect(() => {
		player.eventMap[`addOn${capitalize(event)}Listener`](callback as any);
		return () =>
			player.eventMap[`removeOn${capitalize(event)}Listener`](callback as any);
	}, [player, event, callback]);
};

export const usePlayerState = <Key extends keyof OmniPlayerState>(
	key: Key,
): OmniPlayerState[Key] => {
	const player = usePlayer();
	const [ret, setState] = useState(player[key]);
	// TODO: find a way to listen to that.
	return ret;
};
