(ns search-engine.index)

;;
;; PAGE INDEX
;;

(def indexed-pages (atom #{}))

(defn add-to-page-index! [page]
  (swap! indexed-pages conj page))

(defn page-indexed? [page]
  (contains? @indexed-pages page))

;;
;; WORD INDEX
;;

;; store a coll of [word page location] tuples.
(def indexed-words (atom []))

(defn add-to-word-index! [page words]
  (doall
   (for [el (map-indexed (fn [idx word]
                           (vector word page idx))
                         words)]
     (swap! indexed-words conj el)))
  (println (str (count words) " word(s) indexed")))

(defn lookup-at-word-index [word]
  ;; the first tuple position is the word
  (map #(when (= (first %) word) %) @indexed-words))
