(ns status-im.chat.models.images
  (:require [re-frame.core :as re-frame]
            [utils.re-frame :as rf]
            ["@react-native-community/cameraroll" :as CameraRoll]
            ["react-native-blob-util" :default ReactNativeBlobUtil]
            [status-im.utils.types :as types]
            [status-im.utils.config :as config]
            [status-im.ui.components.permissions :as permissions]
            [status-im.ui.components.react :as react]
            [status-im.utils.image-processing :as image-processing]
            [taoensso.timbre :as log]
            [clojure.string :as string]
            [status-im.i18n.i18n :as i18n]
            [status-im.utils.utils :as utils]
            [status-im.utils.platform :as platform]
            [status-im.utils.fs :as fs]
            [status-im.chat.models :as chat]))

(def maximum-image-size-px 2000)

(defn- resize-and-call [uri cb]
  (react/image-get-size
   uri
   (fn [width height]
     (let [resize? (> (max width height) maximum-image-size-px)]
       (image-processing/resize
        uri
        (if resize? maximum-image-size-px width)
        (if resize? maximum-image-size-px height)
        60
        (fn [^js resized-image]
          (let [path (.-path resized-image)
                path (if (string/starts-with? path "file") path (str "file://" path))]
            (cb path)))
        #(log/error "could not resize image" %))))))

(defn result->id [^js result]
  (if platform/ios?
    (.-localIdentifier result)
    (.-path result)))

(def temp-image-url (str (fs/cache-dir) "/StatusIm_Image.jpeg"))

(defn download-image-http [base64-uri on-success]
  (-> (.config ReactNativeBlobUtil (clj->js {:trusty platform/ios?
                                             :path temp-image-url}))
      (.fetch "GET" base64-uri)
      (.then #(on-success (.path %)))
      (.catch #(log/error "could not save image"))))

(defn save-to-gallery [path] (.save CameraRoll path))

(re-frame/reg-fx
 ::save-image-to-gallery
 (fn [base64-uri]
   (if platform/ios?
     (-> (download-image-http base64-uri save-to-gallery)
         (.catch #(utils/show-popup (i18n/label :t/error)
                                    (i18n/label :t/external-storage-denied))))
     (permissions/request-permissions
      {:permissions [:write-external-storage]
       :on-allowed  #(download-image-http base64-uri save-to-gallery)
       :on-denied   (fn []
                      (utils/set-timeout
                       #(utils/show-popup (i18n/label :t/error)
                                          (i18n/label :t/external-storage-denied))
                       50))}))))

(re-frame/reg-fx
 ::chat-open-image-picker-camera
 (fn [current-chat-id]
   (react/show-image-picker-camera
    #(re-frame/dispatch [:chat.ui/image-captured current-chat-id (.-path %)]) {})))

(re-frame/reg-fx
 ::chat-open-image-picker
 (fn [chat-id]
   (react/show-image-picker
    (fn [^js images]
      ;; NOTE(Ferossgp): Because we can't highlight the already selected images inside
      ;; gallery, we just clean previous state and set all newly picked images
      (when (and platform/ios? (pos? (count images)))
        (re-frame/dispatch [:chat.ui/clear-sending-images chat-id]))
      (doseq [^js result (if platform/ios?
                           (take config/max-images-batch images)
                           [images])]
        (resize-and-call (.-path result)
                         #(re-frame/dispatch [:chat.ui/image-selected chat-id (result->id result) %]))))
    ;; NOTE(Ferossgp): On android you cannot set max limit on images, when a user
    ;; selects too many images the app crashes.
    {:media-type "photo"
     :multiple   platform/ios?})))

(re-frame/reg-fx
 ::image-selected
 (fn [[uri chat-id]]
   (resize-and-call
    uri
    #(re-frame/dispatch [:chat.ui/image-selected chat-id uri %]))))

(re-frame/reg-fx
 ::camera-roll-get-photos
 (fn [num]
   (permissions/request-permissions
    {:permissions [:read-external-storage]
     :on-allowed  (fn []
                    (-> (.getPhotos CameraRoll #js {:first num :assetType "Photos" :groupTypes "All"})
                        (.then #(re-frame/dispatch [:on-camera-roll-get-photos (:edges (types/js->clj %))]))
                        (.catch #(log/warn "could not get cameraroll photos"))))})))

(rf/defn image-captured
  {:events [:chat.ui/image-captured]}
  [{:keys [db]} chat-id uri]
  (let [current-chat-id (or chat-id (:current-chat-id db))
        images          (get-in db [:chat/inputs current-chat-id :metadata :sending-image])]
    (when (and (< (count images) config/max-images-batch)
               (not (get images uri)))
      {::image-selected [uri current-chat-id]})))

(rf/defn camera-roll-get-photos
  {:events [:chat.ui/camera-roll-get-photos]}
  [_ num]
  {::camera-roll-get-photos num})

(rf/defn on-camera-roll-get-photos
  {:events [:on-camera-roll-get-photos]}
  [{db :db} photos]
  {:db (assoc db :camera-roll-photos (mapv #(get-in % [:node :image :uri]) photos))})

(rf/defn clear-sending-images
  {:events [:chat.ui/clear-sending-images]}
  [{:keys [db]} current-chat-id]
  {:db (update-in db [:chat/inputs current-chat-id :metadata] assoc :sending-image {})})

(rf/defn cancel-sending-image
  {:events [:chat.ui/cancel-sending-image]}
  [{:keys [db] :as cofx} chat-id]
  (let [current-chat-id (or chat-id (:current-chat-id db))]
    (clear-sending-images cofx current-chat-id)))

(rf/defn cancel-sending-image-timeline
  {:events [:chat.ui/cancel-sending-image-timeline]}
  [{:keys [db] :as cofx}]
  (cancel-sending-image cofx (chat/my-profile-chat-topic db)))

(rf/defn image-selected
  {:events [:chat.ui/image-selected]}
  [{:keys [db]} current-chat-id original uri]
  {:db (update-in db [:chat/inputs current-chat-id :metadata :sending-image original] merge {:uri uri})})

(rf/defn image-unselected
  {:events [:chat.ui/image-unselected]}
  [{:keys [db]} original]
  (let [current-chat-id (:current-chat-id db)]
    {:db (update-in db [:chat/inputs current-chat-id :metadata :sending-image] dissoc original)}))

(rf/defn chat-open-image-picker
  {:events [:chat.ui/open-image-picker]}
  [{:keys [db]} chat-id]
  (let [current-chat-id (or chat-id (:current-chat-id db))
        images          (get-in db [:chat/inputs current-chat-id :metadata :sending-image])]
    (when (< (count images) config/max-images-batch)
      {::chat-open-image-picker current-chat-id})))

(rf/defn chat-open-image-picker-timeline
  {:events [:chat.ui/open-image-picker-timeline]}
  [{:keys [db] :as cofx}]
  (chat-open-image-picker cofx (chat/my-profile-chat-topic db)))

(rf/defn chat-show-image-picker-camera
  {:events [:chat.ui/show-image-picker-camera]}
  [{:keys [db]} chat-id]
  (let [current-chat-id (or chat-id (:current-chat-id db))
        images          (get-in db [:chat/inputs current-chat-id :metadata :sending-image])]
    (when (< (count images) config/max-images-batch)
      {::chat-open-image-picker-camera current-chat-id})))

(rf/defn chat-show-image-picker-camera-timeline
  {:events [:chat.ui/show-image-picker-camera-timeline]}
  [{:keys [db] :as cofx}]
  (chat-show-image-picker-camera cofx (chat/my-profile-chat-topic db)))

(rf/defn camera-roll-pick
  {:events [:chat.ui/camera-roll-pick]}
  [{:keys [db]} uri chat-id]
  (let [current-chat-id (or chat-id (:current-chat-id db))
        images          (get-in db [:chat/inputs current-chat-id :metadata :sending-image])]
    (if (get-in db [:chats current-chat-id :timeline?])
      {:db              (assoc-in db [:chat/inputs current-chat-id :metadata :sending-image] {})
       ::image-selected [uri current-chat-id]}
      (when (and (< (count images) config/max-images-batch)
                 (not (get images uri)))
        {::image-selected [uri current-chat-id]}))))

(rf/defn camera-roll-pick-timeline
  {:events [:chat.ui/camera-roll-pick-timeline]}
  [{:keys [db] :as cofx} uri]
  (camera-roll-pick cofx uri (chat/my-profile-chat-topic db)))

(rf/defn save-image-to-gallery
  {:events [:chat.ui/save-image-to-gallery]}
  [_ base64-uri]
  {::save-image-to-gallery base64-uri})
