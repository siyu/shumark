(ns Shumark.util.mapper)

(defn reduce-rows
  "Reduce rows by specifying parent keys and children keys.
   only works for two level.
   m contains :parent-keys and :children-keys"
  [{:keys [parent-keys children-keys] :as m} coll]
  (->> coll
       (group-by #(select-keys % parent-keys))
       (map (fn [[ k v]] (assoc k :children (map #(select-keys % children-keys) v))))))

