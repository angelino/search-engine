(ns search-engine.search
  (:require [clojure.string :as str]
            [search-engine.index :as index]))

(defn search [text]
  (let [words (str/split text #" ")]
    (map index/lookup-at-word-index words)))
