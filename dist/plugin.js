var capacitorAudioFromVideoRetriever = (function (exports, core) {
    'use strict';

    const AudioFromVideoRetriever = core.registerPlugin('AudioFromVideoRetriever', {
        web: () => Promise.resolve().then(function () { return web; }).then(m => new m.AudioFromVideoRetrieverWeb()),
    });

    class AudioFromVideoRetrieverWeb extends core.WebPlugin {
        async compressVideo(options) {
            console.log('ECHO', options);
            return { path: options.path };
        }
        async extractAudio(options) {
            console.log('ECHO', options);
            return options;
        }
    }

    var web = /*#__PURE__*/Object.freeze({
        __proto__: null,
        AudioFromVideoRetrieverWeb: AudioFromVideoRetrieverWeb
    });

    exports.AudioFromVideoRetriever = AudioFromVideoRetriever;

    Object.defineProperty(exports, '__esModule', { value: true });

    return exports;

})({}, capacitorExports);
//# sourceMappingURL=plugin.js.map
