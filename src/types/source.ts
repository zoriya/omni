export interface Source {
	src: VideoSrc[];
	startTime?: number;
	subtitles: Subtitle[];
	metadata?: Metadata;
	mixAudio?: MixAudioMode;
}

export interface VideoSrc {
	uri: string;
	mimeType?: string;
	// header sends with xhr requests (especially useful for HLS)
	// might not be available depending on browser/platform.
	headers: Record<string, string>;
}

export interface Subtitle {
	id: string;
	link: string;
	mimeType?: string;
	language?: string;
	label?: string;
}

export interface Metadata {
	title: string;
	album?: string;
	artist?: string;
	imageLink?: string;
	// if one of those is true, the prev/next button will be there and a `onPrevSelected` even will be fired.
	hasPrev?: boolean;
	hasNext?: boolean;
}

export type MixAudioMode = "mixWithOthers" | "doNotMix" | "duckOthers" | "auto";
