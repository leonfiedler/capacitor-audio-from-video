import { WebPlugin } from '@capacitor/core';
export class AudioFromVideoRetrieverWeb extends WebPlugin {
    async compressVideo(options) {
        console.log('ECHO', options);
        return { path: options.path };
    }
    async extractAudio(options) {
        console.log('ECHO', options);
        return options;
    }
}
//# sourceMappingURL=web.js.map