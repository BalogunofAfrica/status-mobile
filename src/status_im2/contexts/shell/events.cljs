(ns status-im2.contexts.shell.events
  (:require [utils.re-frame :as rf]
            [re-frame.core :as re-frame]
            [status-im.utils.core :as utils]
            [status-im2.constants :as constants]
            [status-im2.contexts.shell.state :as state]
            [status-im2.contexts.shell.utils :as shell.utils]
            [status-im2.navigation.state :as navigation.state]
            [status-im2.contexts.shell.animation :as animation]
            [status-im2.contexts.shell.constants :as shell.constants]
            [status-im.data-store.switcher-cards :as switcher-cards-store]))

;;;; Effects

;; Navigation
(re-frame/reg-fx
 :shell/change-tab-fx
 (fn [stack-id]
   (when (some #(= stack-id %) shell.constants/stacks-ids)
     (animation/bottom-tab-on-press stack-id false))))

(re-frame/reg-fx
 :shell/navigate-back
 (fn [view-id]
   (animation/animate-floating-screen
    view-id
    {:animation shell.constants/close-screen-with-slide-animation})))

(re-frame/reg-fx
 :shell/navigate-to-jump-to-fx
 (fn []
   (animation/close-home-stack false)
   (some-> ^js @state/jump-to-list-ref
           (.scrollToOffset #js {:y 0 :animated false}))))

;;;; Events

;; Switcher
(rf/defn switcher-cards-loaded
  {:events [:shell/switcher-cards-loaded]}
  [{:keys [db]} loaded-switcher-cards]
  {:db (assoc db
              :shell/switcher-cards
              (utils/index-by :card-id (switcher-cards-store/<-rpc loaded-switcher-cards)))})

(defn calculate-card-data
  [db now view-id id]
  (case view-id
    :chat
    (let [chat (get-in db [:chats id])]
      (case (:chat-type chat)
        constants/one-to-one-chat-type
        {:card-id       id
         :switcher-card {:type      shell.constants/one-to-one-chat-card
                         :card-id   id
                         :clock     now
                         :screen-id id}}

        constants/private-group-chat-type
        {:card-id       id
         :switcher-card {:type      shell.constants/private-group-chat-card
                         :card-id   id
                         :clock     now
                         :screen-id id}}

        constants/community-chat-type
        {:card-id       (:community-id chat)
         :switcher-card {:type      shell.constants/community-channel-card
                         :card-id   (:community-id chat)
                         :clock     now
                         :screen-id (:chat-id chat)}}

        nil))

    :community-overview
    {:card-id       id
     :switcher-card {:type      shell.constants/community-card
                     :card-id   id
                     :clock     now
                     :screen-id id}}
    nil))

(rf/defn add-switcher-card
  {:events [:shell/add-switcher-card]}
  [{:keys [db now] :as cofx} view-id id]
  (let [card-data     (calculate-card-data db now view-id id)
        switcher-card (:switcher-card card-data)]
    (when card-data
      (rf/merge
       cofx
       {:db (assoc-in
             db
             [:shell/switcher-cards (:card-id card-data)]
             switcher-card)}
       (switcher-cards-store/upsert-switcher-card-rpc switcher-card)))))

(rf/defn close-switcher-card
  {:events [:shell/close-switcher-card]}
  [{:keys [db] :as cofx} card-id]
  (rf/merge
   cofx
   {:db (update db :shell/switcher-cards dissoc card-id)}
   (switcher-cards-store/delete-switcher-card-rpc card-id)))

;; Navigation
(rf/defn navigate-to-jump-to
  {:events [:shell/navigate-to-jump-to]}
  [{:keys [db]}]
  (let [current-view-id (:view-id db)
        current-chat-id (:current-chat-id db)
        community-chat? (when current-chat-id
                          (= (get-in db [:chats current-chat-id :chat-type])
                             constants/community-chat-type))]
    {:db
     (cond-> db

       (= current-view-id shell.constants/chat-screen)
       (assoc-in [:shell/floating-screens shell.constants/chat-screen :animation]
        shell.constants/close-screen-with-shell-animation)

       community-chat?
       (assoc-in [:shell/floating-screens shell.constants/community-screen :animation]
        shell.constants/close-screen-without-animation)

       (= current-view-id shell.constants/community-screen)
       (assoc-in [:shell/floating-screens shell.constants/community-screen :animation]
        shell.constants/close-screen-with-shell-animation))

     :dispatch [:set-view-id :shell]
     :shell/navigate-to-jump-to-fx nil}))

(rf/defn change-shell-status-bar-style
  {:events [:change-shell-status-bar-style]}
  [_ style]
  {:merge-options {:id "shell-stack" :options {:statusBar {:style style}}}})

(rf/defn change-shell-nav-bar-color
  {:events [:change-shell-nav-bar-color]}
  [_ color]
  {:merge-options {:id "shell-stack" :options {:navigationBar {:backgroundColor color}}}})

(rf/defn shell-navigate-to
  {:events [:shell/navigate-to]}
  [{:keys [db]} go-to-view-id screen-params animation hidden-screen?]
  (if (shell.utils/shell-navigation? go-to-view-id)
    (let [current-view-id (:view-id db)
          community-id    (get-in db [:chats screen-params :community-id])]
      (merge
       {:db (assoc-in
             db
             [:shell/floating-screens go-to-view-id]
             {:id              screen-params
              :community-id    community-id
              :current-view-id current-view-id
              :hidden-screen?  hidden-screen?
              :animation       (or animation
                                   (if (= current-view-id :shell)
                                     shell.constants/open-screen-with-shell-animation
                                     shell.constants/open-screen-with-slide-animation))})}
       (when-not hidden-screen? {:dispatch-n [[:set-view-id go-to-view-id]]})))
    {:db          (assoc db :view-id go-to-view-id)
     :navigate-to go-to-view-id}))

(rf/defn shell-navigate-back
  {:events [:shell/navigate-back]}
  [{:keys [db]}]
  (let [current-view-id (:view-id db)
        current-chat-id (:current-chat-id db)
        community-id    (when current-chat-id
                          (get-in db [:chats current-chat-id :community-id]))]
    (if (and (not @navigation.state/curr-modal)
             (shell.utils/shell-navigation? current-view-id))
      {:db         (assoc-in
                    db
                    [:shell/floating-screens current-view-id :animation]
                    shell.constants/close-screen-with-slide-animation)
       :dispatch-n (cond-> [[:set-view-id
                             (cond
                               (= current-view-id shell.constants/community-screen)
                               :communities-stack
                               (shell.utils/floating-screen-open? shell.constants/community-screen)
                               shell.constants/community-screen
                               :else :chats-stack)]]
                     ;; When navigating back from community chat to community, update switcher card
                     (and (= current-view-id shell.constants/chat-screen) community-id)
                     (conj [:shell/add-switcher-card shell.constants/community-screen community-id]))}
      {:navigate-back nil})))
