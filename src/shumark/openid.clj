(ns shumark.openid
  "http://code.google.com/p/openid4java/
   http://davidtanzer.net/clojure_openid
   http://sureshatt.blogspot.com/2011/05/openid-consumer-for-attribute-exchange.html"
  (use [ring.util.response :only [redirect]])
  (import (org.openid4java.consumer ConsumerManager)
          (org.openid4java.message ParameterList)
          (org.openid4java.message.ax FetchRequest AxMessage)))

(def ^:private consumer-mgr (ConsumerManager.))

(defn redirect->openid [req return-path]
  (let [oid-url "https://www.google.com/accounts/o8/id"        
        return-url (str (-> :scheme req name) "://" (get-in req [:headers "host"]) return-path)
        discoveries (.discover consumer-mgr oid-url)
        discovered (.associate consumer-mgr discoveries)
        fetch-req (doto (FetchRequest/createFetchRequest)
                       (.addAttribute "email""http://axschema.org/contact/email" true))
        auth-req (doto (.authenticate consumer-mgr discovered return-url)
                      (.addExtension fetch-req))
        dest-url (.getDestinationUrl auth-req true)]
    (assoc (redirect dest-url)
      :session (assoc (:session req) :openid-discovered discovered))))

(defn verify [{:keys [params session] :as req} login-success-handler login-failure-handler]
  (let [plist (ParameterList. params)
        discovered (get-in req [:session :openid-discovered])
        receiving-url (str (name (:scheme req)) "://"
                          ((:headers req) "host")
                          (:uri req)
                          "?" (:query-string req))
        verification (.verify consumer-mgr receiving-url plist
                              discovered)
        verified-id (.getVerifiedId verification)]
    (if verified-id
      (let [auth-success (.getAuthResponse verification)
            fetch-resp (.getExtension auth-success AxMessage/OPENID_NS_AX)
            email (.getAttributeValue fetch-resp "email")]
        (-> req
            (assoc :session (assoc session 
                              :openid-discovered nil
                              :auth-map {:email email}))
            login-success-handler))
      (login-failure-handler req))))
