(ns search-engine.core
  (:require [clojure.string :as str]
            [clj-http.client :as client]
            [hickory.core :as html]
            [hickory.select :as s]))

(def indexed-pages (atom #{}))
(def indexed-words (atom {}))

(defn page-indexed? [page]
  (contains? @indexed-pages page))

(defn download [page]
  (try
    (client/get page)
    (catch Exception ex
      (println (str "It was not possible download " page)))))

(defn find-links [document]
  (let [links (s/select (s/tag :a) (html/as-hickory document))]
    (filter #(str/starts-with? % "http")
            (map :href (filter #(not (nil? (:href %)))
                               (map :attrs links))))))

(defn get-only-text [document]
  (let [elements (s/select (s/node-type :element) (html/as-hickory document))
        texts (filter string?
                      (map #(first (:content %)) elements))]
    (clojure.string/join " " texts)))

(defn separate-words [text]
  (clojure.string/split (.toLowerCase text) #"\W+"))

(defn add-index [page document]
  (println (str "Indexing: " page))
  (let [text (get-only-text document)
        words (separate-words text)]
    (println (frequencies words))
    (swap! indexed-pages conj page)))

(defn crawl-page [page]
  (if-let [content (:body (download page))]
    (if-let [document (html/parse content)]
      (do
        (add-index page document)
        (find-links document)))))

(defn not-indexed-pages [pages]
  (filter #(not (page-indexed? %)) pages))

(defn crawl-pages [pages]
  (let [new-pages (atom #{})]
    (doall
     (for [page (not-indexed-pages pages)]
       (do
         (let [links (set (crawl-page page))]
           (printf "Links found: %s\n" (count links))
           (swap! new-pages clojure.set/union links)))))
    @new-pages))

(defn crawl [pages depth]
  (let [pgs (atom (set pages))]
    (doall
     (for [i (range depth)]
       (reset! pgs (crawl-pages @pgs))))))

(comment
  (crawl-page "https://pt.wikipedia.org/wiki/Wikipédia:Página_principal")
  (crawl ["https://pt.wikipedia.org/wiki/Wikipédia:Página_principal"] 2))

