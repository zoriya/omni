import { getHostComponent, type HybridRef } from "react-native-nitro-modules";
import OmniConfig from "../nitrogen/generated/shared/json/OmniConfig.json";
import type { OmniMethods, OmniProps } from "./specs/omni.nitro";

export const Omni = getHostComponent<OmniProps, OmniMethods>(
	"Omni",
	() => OmniConfig,
);

export type OmniRef = HybridRef<OmniProps, OmniMethods>;
