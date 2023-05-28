(ns status-im2.contexts.chat.messages.view
  (:require [quo2.core :as quo]
            [re-frame.db]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [status-im2.constants :as constants]
            [status-im2.contexts.chat.composer.view :as composer]
            [status-im2.contexts.chat.messages.contact-requests.bottom-drawer :as
             contact-requests.bottom-drawer]
            [status-im2.contexts.chat.messages.list.view :as messages.list]
            [status-im2.contexts.chat.messages.pin.banner.view :as pin.banner]
            [utils.debounce :as debounce]
            [utils.re-frame :as rf]))

(defn page-nav
  []
  (let [{:keys [group-chat chat-id chat-name emoji
                chat-type]} (rf/sub [:chats/current-chat])
        display-name        (if (= chat-type constants/one-to-one-chat-type)
                              (first (rf/sub [:contacts/contact-two-names-by-identity chat-id]))
                              (str emoji " " chat-name))
        online?             (rf/sub [:visibility-status-updates/online? chat-id])
        contact             (when-not group-chat
                              (rf/sub [:contacts/contact-by-address chat-id]))
        photo-path          (rf/sub [:chats/photo-path chat-id])
        avatar-image-key    (if (seq (:images contact))
                              :profile-picture
                              :ring-background)]
    [quo/page-nav
     {:align-mid?            true
      :mid-section           (if group-chat
                               {:type      :text-only
                                :main-text display-name}
                               {:type      :user-avatar
                                :avatar    {:full-name       display-name
                                            :online?         online?
                                            :size            :medium
                                            avatar-image-key photo-path}
                                :main-text display-name
                                :on-press  #(debounce/dispatch-and-chill [:chat.ui/show-profile chat-id]
                                                                         1000)})

      :left-section          {:on-press            #(rf/dispatch [:navigate-back])
                              :icon                :i/arrow-left
                              :accessibility-label :back-button}

      :right-section-buttons [{:on-press            #()
                               :style               {:border-width 1
                                                     :border-color :red}
                               :icon                :i/options
                               :accessibility-label :options-button}]}]))

(defn chat
  []
  (let [;;NOTE: we want to react only on these fields, do not use full chat map here
        {:keys [chat-id contact-request-state group-chat able-to-send-message?] :as chat}
        (rf/sub [:chats/current-chat-chat-view])
        insets (safe-area/get-insets)]
    ;; Lazy load some of stuff, and use loading skeleton in meantime
    ;; Composer is heaviest and taking most of time, so maybe delay loading of that
    ;; Once removed composer, chat opening is super fast
    [rn/keyboard-avoiding-view
     {:style                  {:position   :relative
                               :flex       1
                               :margin-top (:top insets)}
      :keyboardVerticalOffset (- (:bottom insets))
      :behavior               :height}
     [page-nav]
     [pin.banner/banner chat-id]
     [messages.list/messages-list chat insets]
     (if-not able-to-send-message?
       [contact-requests.bottom-drawer/view chat-id contact-request-state group-chat]
       [:f> composer/composer insets])]))
