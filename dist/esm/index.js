import { registerPlugin } from '@capacitor/core';
const AudioFromVideoRetriever = registerPlugin('AudioFromVideoRetriever', {
    web: () => import('./web').then(m => new m.AudioFromVideoRetrieverWeb()),
});
export * from './definitions';
export { AudioFromVideoRetriever };
//# sourceMappingURL=index.js.map