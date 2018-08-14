(ns search-engine.core
  (:require [clojure.string :as str]
            [clj-http.client :as client]
            [hickory.core :as html]
            [hickory.select :as s]))

(def indexed-pages (atom #{}))

(defn add-index [page]
  (swap! indexed-pages conj page)
  (println (str "Indexed: " page)))

(defn page-indexed? [page]
  (contains? @indexed-pages page))
  
(defn download [page]
  (client/get page))

(defn find-links [content]
  (let [document (html/parse content)
        links (s/select (s/tag :a) (html/as-hickory document))]
    (filter #(str/starts-with? % "http")
            (map :href (filter #(not (nil? (:href %)))
                               (map :attrs links))))))

(defn crawl-page [page]
  (if-let [content (:body (download page))]
    (do
      (add-index page)
      (find-links content))))

(defn not-indexed-pages [pages]
  (filter #(not (page-indexed? %)) pages))

(defn crawl-pages! [pages]
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
       (reset! pgs (crawl-pages! @pgs))))))

(comment
  (crawl-page "https://pt.wikipedia.org/wiki/Wikipédia:Página_principal")
  (crawl ["http://www.google.com"] 2)
)

; find the links
