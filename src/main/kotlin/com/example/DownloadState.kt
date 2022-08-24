package com.example

enum class DownloadState {
    DOWNLOADING {
        override fun toString(): String {
            return "Downloading"
        }
    },
    PAUSED {
        override fun toString(): String {
            return "Paused"
        }
    },
    STOPPED {
        override fun toString(): String {
            return "Stopped"
        }
    }
}