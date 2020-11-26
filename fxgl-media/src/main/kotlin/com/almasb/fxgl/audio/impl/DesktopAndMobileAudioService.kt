/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package com.almasb.fxgl.audio.impl

import com.almasb.fxgl.audio.Audio
import com.almasb.fxgl.audio.AudioType
import com.almasb.fxgl.logging.Logger
import com.gluonhq.attach.audio.AudioService
import javafx.scene.media.AudioClip
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import java.net.URL

/**
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class DesktopAndMobileAudioService : DefaultAudioService() {

    companion object {
        private val log = Logger.get<DesktopAndMobileAudioService>()
        private val attachService = AudioService.create()
    }

    override fun loadAudioImpl(type: AudioType, resourceURL: URL, isDesktop: Boolean): Audio {
        if (isDesktop) {
            val url = resourceURL.toExternalForm()

            return if (type === AudioType.MUSIC) {
                DesktopMusic(url, MediaPlayer(Media(url)))
            } else {
                DesktopSound(url, AudioClip(url))
            }
        }

        if (attachService.isPresent) {
            val nativeAudio = if (type === AudioType.MUSIC) {
                attachService.get().loadMusic(resourceURL)
            } else {
                attachService.get().loadSound(resourceURL)
            }

            if (nativeAudio.isPresent) {
                log.debug("Attach Audio correctly loaded: $resourceURL")
            } else {
                log.warning("Attach Audio could not load: $resourceURL")
            }

            return object : Audio(type, resourceURL.toExternalForm()) {
                override fun setLooping(looping: Boolean) {
                    nativeAudio.ifPresent { it.setLooping(looping) }
                }

                override fun setVolume(volume: Double) {
                    nativeAudio.ifPresent { it.setVolume(volume) }
                }

                override fun setOnFinished(action: Runnable) {
                    // no-op
                }

                override fun play() {
                    nativeAudio.ifPresent { it.play() }
                }

                override fun pause() {
                    nativeAudio.ifPresent { it.pause() }
                }

                override fun stop() {
                    nativeAudio.ifPresent { it.stop() }
                }

                override fun dispose() {
                    nativeAudio.ifPresent { it.dispose() }
                }
            }
        }

        log.warning("Attach Audio not present")

        throw IllegalStateException("Attach Audio not present")
    }
}