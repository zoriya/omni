import { type RefObject, useRef } from "react";

const empty = Symbol("useLazyRef empty value");

export const useLazyRef = <T>(init: () => T): RefObject<T> => {
	const resultRef = useRef<T | typeof empty>(empty);

	if (resultRef.current === empty) {
		resultRef.current = init();
	}

	return resultRef as RefObject<T>;
};
