import { WebPlugin } from '@capacitor/core';
import type { AudioFromVideoRetrieverPlugin } from './definitions';
export declare class AudioFromVideoRetrieverWeb extends WebPlugin implements AudioFromVideoRetrieverPlugin {
    compressVideo(options: {
        path: string;
        outputPath: string;
        width: number;
        height: number;
        bitrate: number;
    }): Promise<{
        path: string;
    }>;
    extractAudio(options: {
        path: string;
        outputPath: string;
        includeData?: boolean | undefined;
    }): Promise<{
        path: string;
        dataUrl?: string;
    }>;
}
