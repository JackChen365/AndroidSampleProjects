package com.cz.android.animator.version3;

import android.view.animation.Interpolator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Created by cz
 * @date 2020/5/29 3:02 PM
 * @email bingo110@126.com
 */
public class SimpleAnimatorSet extends SimpleAnimator {
    /**
     * Internal variables
     * NOTE: This object implements the clone() method, making a deep copy of any referenced
     * objects. As other non-trivial fields are added to this class, make sure to add logic
     * to clone() to make deep copies of them.
     */

    /**
     * Tracks animations currently being played, so that we know what to
     * cancel or end when cancel() or end() is called on this AnimatorSet
     */
    private ArrayList<SimpleValueAnimator> mPlayingSet = new ArrayList<SimpleValueAnimator>();

    /**
     * Contains all nodes, mapped to their respective Animators. When new
     * dependency information is added for an Animator, we want to add it
     * to a single node representing that Animator, not create a new Node
     * if one already exists.
     */
    private HashMap<SimpleValueAnimator, Node> mNodeMap = new HashMap<SimpleValueAnimator, Node>();

    /**
     * Set of all nodes created for this AnimatorSet. This list is used upon
     * starting the set, and the nodes are placed in sorted order into the
     * sortedNodes collection.
     */
    private ArrayList<Node> mNodes = new ArrayList<Node>();

    /**
     * The sorted list of nodes. This is the order in which the animations will
     * be played. The details about when exactly they will be played depend
     * on the dependency relationships of the nodes.
     */
    private ArrayList<Node> mSortedNodes = new ArrayList<Node>();

    /**
     * Flag indicating whether the nodes should be sorted prior to playing. This
     * flag allows us to cache the previous sorted nodes so that if the sequence
     * is replayed with no changes, it does not have to re-sort the nodes again.
     */
    private boolean mNeedsSort = true;

    /**
     * Flag indicating that the AnimatorSet has been manually
     * terminated (by calling cancel() or end()).
     * This flag is used to avoid starting other animations when currently-playing
     * child animations of this AnimatorSet end. It also determines whether cancel/end
     * notifications are sent out via the normal AnimatorSetListener mechanism.
     */
    boolean mTerminated = false;
    // Animator used for a nonzero startDelay
    private SimpleAnimator mDelayAnim = null;
    /**
     * Indicates whether an AnimatorSet has been start()'d, whether or
     * not there is a nonzero startDelay.
     */
    private boolean mStarted = false;

    // The amount of time in ms to delay starting the animation after start() is called
    private long mStartDelay = 0;

    // How long the child animations should last in ms. The default value is negative, which
    // simply means that there is no duration set on the AnimatorSet. When a real duration is
    // set, it is passed along to the child animations.
    private long mDuration = -1;
    private AnimatorSetListener mSetListener = null;

    /**
     * {@inheritDoc}
     *
     * <p>Starting this <code>AnimatorSet</code> will, in turn, start the animations for which
     * it is responsible. The details of when exactly those animations are started depends on
     * the dependency relationships that have been set up between the animations.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void start() {
        mTerminated = false;
        mStarted = true;

        // First, sort the nodes (if necessary). This will ensure that sortedNodes
        // contains the animation nodes in the correct order.
        sortNodes();

        int numSortedNodes = mSortedNodes.size();
        // nodesToStart holds the list of nodes to be started immediately. We don't want to
        // start the animations in the loop directly because we first need to set up
        // dependencies on all of the nodes. For example, we don't want to start an animation
        // when some other animation also wants to start when the first animation begins.
        final ArrayList<Node> nodesToStart = new ArrayList<Node>();
        for (int i = 0; i < numSortedNodes; ++i) {
            Node node = mSortedNodes.get(i);
            if (node.dependencies == null || node.dependencies.size() == 0) {
                nodesToStart.add(node);
            } else {
                int numDependencies = node.dependencies.size();
                for (int j = 0; j < numDependencies; ++j) {
                    Dependency dependency = node.dependencies.get(j);
                    dependency.node.animation.addListener(
                            new DependencyListener(this, node, dependency.rule));
                }
                node.tmpDependencies = (ArrayList<Dependency>) node.dependencies.clone();
            }
            node.animation.addListener(mSetListener);
        }
        // Now that all dependencies are set up, start the animations that should be started.
        if (mStartDelay <= 0) {
            for (Node node : nodesToStart) {
                node.animation.start();
                mPlayingSet.add(node.animation);
            }
        } else {
            mDelayAnim = new SimpleValueAnimator();
            mDelayAnim.setDuration(mStartDelay);
            mDelayAnim.addListener(new AnimatorListener() {
                boolean canceled = false;
                public void onAnimationCancel(SimpleAnimator anim) {
                    canceled = true;
                }

                @Override
                public void onAnimationRepeat(SimpleAnimator animation) {
                }

                @Override
                public void onAnimationStart(SimpleAnimator animation) {
                }

                public void onAnimationEnd(SimpleAnimator anim) {
                    if (!canceled) {
                        int numNodes = nodesToStart.size();
                        for (int i = 0; i < numNodes; ++i) {
                            Node node = nodesToStart.get(i);
                            node.animation.start();
                            mPlayingSet.add(node.animation);
                        }
                    }
                }
            });
            mDelayAnim.start();
        }
        if (mListeners != null) {
            ArrayList<AnimatorListener> tmpListeners =
                    (ArrayList<AnimatorListener>) mListeners.clone();
            int numListeners = tmpListeners.size();
            for (int i = 0; i < numListeners; ++i) {
                tmpListeners.get(i).onAnimationStart(this);
            }
        }
        if (mNodes.size() == 0 && mStartDelay == 0) {
            // Handle unusual case where empty AnimatorSet is started - should send out
            // end event immediately since the event will not be sent out at all otherwise
            mStarted = false;
            if (mListeners != null) {
                ArrayList<AnimatorListener> tmpListeners =
                        (ArrayList<AnimatorListener>) mListeners.clone();
                int numListeners = tmpListeners.size();
                for (int i = 0; i < numListeners; ++i) {
                    tmpListeners.get(i).onAnimationEnd(this);
                }
            }
        }
    }

    /**
     * This method sorts the current set of nodes, if needed. The sort is a simple
     * DependencyGraph sort, which goes like this:
     * - All nodes without dependencies become 'roots'
     * - while roots list is not null
     * -   for each root r
     * -     add r to sorted list
     * -     remove r as a dependency from any other node
     * -   any nodes with no dependencies are added to the roots list
     */
    private void sortNodes() {
        if (mNeedsSort) {
            mSortedNodes.clear();
            ArrayList<Node> roots = new ArrayList<Node>();
            int numNodes = mNodes.size();
            for (int i = 0; i < numNodes; ++i) {
                Node node = mNodes.get(i);
                if (node.dependencies == null || node.dependencies.size() == 0) {
                    roots.add(node);
                }
            }
            ArrayList<Node> tmpRoots = new ArrayList<Node>();
            while (roots.size() > 0) {
                int numRoots = roots.size();
                for (int i = 0; i < numRoots; ++i) {
                    Node root = roots.get(i);
                    mSortedNodes.add(root);
                    if (root.nodeDependents != null) {
                        int numDependents = root.nodeDependents.size();
                        for (int j = 0; j < numDependents; ++j) {
                            Node node = root.nodeDependents.get(j);
                            node.nodeDependencies.remove(root);
                            if (node.nodeDependencies.size() == 0) {
                                tmpRoots.add(node);
                            }
                        }
                    }
                }
                roots.clear();
                roots.addAll(tmpRoots);
                tmpRoots.clear();
            }
            mNeedsSort = false;
            if (mSortedNodes.size() != mNodes.size()) {
                throw new IllegalStateException("Circular dependencies cannot exist"
                        + " in AnimatorSet");
            }
        } else {
            // Doesn't need sorting, but still need to add in the nodeDependencies list
            // because these get removed as the event listeners fire and the dependencies
            // are satisfied
            int numNodes = mNodes.size();
            for (int i = 0; i < numNodes; ++i) {
                Node node = mNodes.get(i);
                if (node.dependencies != null && node.dependencies.size() > 0) {
                    int numDependencies = node.dependencies.size();
                    for (int j = 0; j < numDependencies; ++j) {
                        Dependency dependency = node.dependencies.get(j);
                        if (node.nodeDependencies == null) {
                            node.nodeDependencies = new ArrayList<Node>();
                        }
                        if (!node.nodeDependencies.contains(dependency.node)) {
                            node.nodeDependencies.add(dependency.node);
                        }
                    }
                }
                // nodes are 'done' by default; they become un-done when started, and done
                // again when ended
                node.done = false;
            }
        }
    }


    /**
     * Sets up this AnimatorSet to play all of the supplied animations at the same time.
     *
     * @param items The animations that will be started simultaneously.
     */
    public void playTogether(SimpleValueAnimator... items) {
        if (items != null) {
            mNeedsSort = true;
            Builder builder = play(items[0]);
            for (int i = 1; i < items.length; ++i) {
                builder.with(items[i]);
            }
        }
    }

    /**
     * Sets up this AnimatorSet to play all of the supplied animations at the same time.
     *
     * @param items The animations that will be started simultaneously.
     */
    public void playTogether(Collection<SimpleValueAnimator> items) {
        if (items != null && items.size() > 0) {
            mNeedsSort = true;
            Builder builder = null;
            for (SimpleValueAnimator anim : items) {
                if (builder == null) {
                    builder = play(anim);
                } else {
                    builder.with(anim);
                }
            }
        }
    }

    /**
     * Sets up this AnimatorSet to play each of the supplied animations when the
     * previous animation ends.
     *
     * @param items The animations that will be started one after another.
     */
    public void playSequentially(SimpleValueAnimator... items) {
        if (items != null) {
            mNeedsSort = true;
            if (items.length == 1) {
                play(items[0]);
            } else {
                for (int i = 0; i < items.length - 1; ++i) {
                    play(items[i]).before(items[i+1]);
                }
            }
        }
    }

    /**
     * Sets up this AnimatorSet to play each of the supplied animations when the
     * previous animation ends.
     *
     * @param items The animations that will be started one after another.
     */
    public void playSequentially(List<SimpleValueAnimator> items) {
        if (items != null && items.size() > 0) {
            mNeedsSort = true;
            if (items.size() == 1) {
                play(items.get(0));
            } else {
                for (int i = 0; i < items.size() - 1; ++i) {
                    play(items.get(i)).before(items.get(i+1));
                }
            }
        }
    }

    public Builder play(SimpleValueAnimator anim) {
        if (anim != null) {
            mNeedsSort = true;
            return new Builder(anim);
        }
        return null;
    }


    @Override
    public long getStartDelay() {
        return mStartDelay;
    }

    @Override
    public void setStartDelay(long startDelay) {
        this.mStartDelay=startDelay;
    }

    @Override
    public SimpleAnimator setDuration(long duration) {
        if (duration < 0) {
            throw new IllegalArgumentException("duration must be a value of zero or greater");
        }
        for (Node node : mNodes) {
            // TODO: don't set the duration of the timing-only nodes created by AnimatorSet to
            // insert "play-after" delays
            node.animation.setDuration(duration);
        }
        mDuration = duration;
        return this;
    }

    @Override
    public long getDuration() {
        return mDuration;
    }

    @Override
    public void setInterpolator(Interpolator value) {
    }

    @Override
    public boolean isRunning() {
        for (Node node : mNodes) {
            if (node.animation.isRunning()) {
                return true;
            }
        }
        return false;
    }

    /**
     * This class is the mechanism by which animations are started based on events in other
     * animations. If an animation has multiple dependencies on other animations, then
     * all dependencies must be satisfied before the animation is started.
     */
    private static class DependencyListener implements AnimatorListener {

        private SimpleAnimatorSet mAnimatorSet;

        // The node upon which the dependency is based.
        private Node mNode;

        // The Dependency rule (WITH or AFTER) that the listener should wait for on
        // the node
        private int mRule;

        public DependencyListener(SimpleAnimatorSet animatorSet, Node node, int rule) {
            this.mAnimatorSet = animatorSet;
            this.mNode = node;
            this.mRule = rule;
        }

        /**
         * Ignore cancel events for now. We may want to handle this eventually,
         * to prevent follow-on animations from running when some dependency
         * animation is canceled.
         */
        public void onAnimationCancel(SimpleAnimator animation) {
        }

        /**
         * An end event is received - see if this is an event we are listening for
         */
        public void onAnimationEnd(SimpleAnimator animation) {
            if (mRule == Dependency.AFTER) {
                startIfReady(animation);
            }
        }

        /**
         * Ignore repeat events for now
         */
        public void onAnimationRepeat(SimpleAnimator animation) {
        }

        /**
         * A start event is received - see if this is an event we are listening for
         */
        public void onAnimationStart(SimpleAnimator animation) {
            if (mRule == Dependency.WITH) {
                startIfReady(animation);
            }
        }

        /**
         * Check whether the event received is one that the node was waiting for.
         * If so, mark it as complete and see whether it's time to start
         * the animation.
         * @param dependencyAnimation the animation that sent the event.
         */
        private void startIfReady(SimpleAnimator dependencyAnimation) {
            if (mAnimatorSet.mTerminated) {
                // if the parent AnimatorSet was canceled, then don't start any dependent anims
                return;
            }
            Dependency dependencyToRemove = null;
            int numDependencies = mNode.tmpDependencies.size();
            for (int i = 0; i < numDependencies; ++i) {
                Dependency dependency = mNode.tmpDependencies.get(i);
                if (dependency.rule == mRule &&
                        dependency.node.animation == dependencyAnimation) {
                    // rule fired - remove the dependency and listener and check to
                    // see whether it's time to start the animation
                    dependencyToRemove = dependency;
                    dependencyAnimation.removeListener(this);
                    break;
                }
            }
            mNode.tmpDependencies.remove(dependencyToRemove);
            if (mNode.tmpDependencies.size() == 0) {
                // all dependencies satisfied: start the animation
                mNode.animation.start();
                mAnimatorSet.mPlayingSet.add(mNode.animation);
            }
        }

    }

    private class AnimatorSetListener implements AnimatorListener {

        private SimpleAnimatorSet mAnimatorSet;

        AnimatorSetListener(SimpleAnimatorSet animatorSet) {
            mAnimatorSet = animatorSet;
        }

        public void onAnimationCancel(SimpleAnimator animation) {
            if (!mTerminated) {
                // Listeners are already notified of the AnimatorSet canceling in cancel().
                // The logic below only kicks in when animations end normally
                if (mPlayingSet.size() == 0) {
                    if (mListeners != null) {
                        int numListeners = mListeners.size();
                        for (int i = 0; i < numListeners; ++i) {
                            mListeners.get(i).onAnimationCancel(mAnimatorSet);
                        }
                    }
                }
            }
        }

        @SuppressWarnings("unchecked")
        public void onAnimationEnd(SimpleAnimator animation) {
            animation.removeListener(this);
            mPlayingSet.remove(animation);
            Node animNode = mAnimatorSet.mNodeMap.get(animation);
            animNode.done = true;
            if (!mTerminated) {
                // Listeners are already notified of the AnimatorSet ending in cancel() or
                // end(); the logic below only kicks in when animations end normally
                ArrayList<Node> sortedNodes = mAnimatorSet.mSortedNodes;
                boolean allDone = true;
                int numSortedNodes = sortedNodes.size();
                for (int i = 0; i < numSortedNodes; ++i) {
                    if (!sortedNodes.get(i).done) {
                        allDone = false;
                        break;
                    }
                }
                if (allDone) {
                    // If this was the last child animation to end, then notify listeners that this
                    // AnimatorSet has ended
                    if (mListeners != null) {
                        ArrayList<AnimatorListener> tmpListeners =
                                (ArrayList<AnimatorListener>) mListeners.clone();
                        int numListeners = tmpListeners.size();
                        for (int i = 0; i < numListeners; ++i) {
                            tmpListeners.get(i).onAnimationEnd(mAnimatorSet);
                        }
                    }
                    mAnimatorSet.mStarted = false;
                }
            }
        }

        // Nothing to do
        public void onAnimationRepeat(SimpleAnimator animation) {
        }

        // Nothing to do
        public void onAnimationStart(SimpleAnimator animation) {
        }

    }



    /**
     * Dependency holds information about the node that some other node is
     * dependent upon and the nature of that dependency.
     *
     */
    private static class Dependency {
        static final int WITH = 0; // dependent node must start with this dependency node
        static final int AFTER = 1; // dependent node must start when this dependency node finishes

        // The node that the other node with this Dependency is dependent upon
        public Node node;

        // The nature of the dependency (WITH or AFTER)
        public int rule;

        public Dependency(Node node, int rule) {
            this.node = node;
            this.rule = rule;
        }
    }

    private static class Node implements Cloneable {
        public SimpleValueAnimator animation;

        /**
         *  These are the dependencies that this node's animation has on other
         *  nodes. For example, if this node's animation should begin with some
         *  other animation ends, then there will be an item in this node's
         *  dependencies list for that other animation's node.
         */
        public ArrayList<Dependency> dependencies = null;

        /**
         * tmpDependencies is a runtime detail. We use the dependencies list for sorting.
         * But we also use the list to keep track of when multiple dependencies are satisfied,
         * but removing each dependency as it is satisfied. We do not want to remove
         * the dependency itself from the list, because we need to retain that information
         * if the AnimatorSet is launched in the future. So we create a copy of the dependency
         * list when the AnimatorSet starts and use this tmpDependencies list to track the
         * list of satisfied dependencies.
         */
        public ArrayList<Dependency> tmpDependencies = null;

        /**
         * nodeDependencies is just a list of the nodes that this Node is dependent upon.
         * This information is used in sortNodes(), to determine when a node is a root.
         */
        public ArrayList<Node> nodeDependencies = null;

        /**
         * nodeDepdendents is the list of nodes that have this node as a dependency. This
         * is a utility field used in sortNodes to facilitate removing this node as a
         * dependency when it is a root node.
         */
        public ArrayList<Node> nodeDependents = null;

        /**
         * Flag indicating whether the animation in this node is finished. This flag
         * is used by AnimatorSet to check, as each animation ends, whether all child animations
         * are done and it's time to send out an end event for the entire AnimatorSet.
         */
        public boolean done = false;

        /**
         * Constructs the Node with the animation that it encapsulates. A Node has no
         * dependencies by default; dependencies are added via the addDependency()
         * method.
         *
         * @param animation The animation that the Node encapsulates.
         */
        public Node(SimpleValueAnimator animation) {
            this.animation = animation;
        }

        /**
         * Add a dependency to this Node. The dependency includes information about the
         * node that this node is dependency upon and the nature of the dependency.
         * @param dependency
         */
        public void addDependency(Dependency dependency) {
            if (dependencies == null) {
                dependencies = new ArrayList<Dependency>();
                nodeDependencies = new ArrayList<Node>();
            }
            dependencies.add(dependency);
            if (!nodeDependencies.contains(dependency.node)) {
                nodeDependencies.add(dependency.node);
            }
            Node dependencyNode = dependency.node;
            if (dependencyNode.nodeDependents == null) {
                dependencyNode.nodeDependents = new ArrayList<Node>();
            }
            dependencyNode.nodeDependents.add(this);
        }

        @Override
        public Node clone() {
            try {
                Node node = (Node) super.clone();
                node.animation = animation.clone();
                return node;
            } catch (CloneNotSupportedException e) {
                throw new AssertionError();
            }
        }
    }

    public class Builder {
        private Node mCurrentNode;

        Builder(SimpleValueAnimator anim) {
            mCurrentNode = mNodeMap.get(anim);
            if (mCurrentNode == null) {
                mCurrentNode = new Node(anim);
                mNodeMap.put(anim, mCurrentNode);
                mNodes.add(mCurrentNode);
            }
        }

        public Builder with(SimpleValueAnimator anim) {
            Node node = mNodeMap.get(anim);
            if (node == null) {
                node = new Node(anim);
                mNodeMap.put(anim, node);
                mNodes.add(node);
            }
            Dependency dependency = new Dependency(mCurrentNode, Dependency.WITH);
            node.addDependency(dependency);
            return this;
        }

        public Builder before(SimpleValueAnimator anim) {
            Node node = mNodeMap.get(anim);
            if (node == null) {
                node = new Node(anim);
                mNodeMap.put(anim, node);
                mNodes.add(node);
            }
            Dependency dependency = new Dependency(mCurrentNode, Dependency.AFTER);
            node.addDependency(dependency);
            return this;
        }

        public Builder after(SimpleValueAnimator anim) {
            Node node = mNodeMap.get(anim);
            if (node == null) {
                node = new Node(anim);
                mNodeMap.put(anim, node);
                mNodes.add(node);
            }
            Dependency dependency = new Dependency(node, Dependency.AFTER);
            mCurrentNode.addDependency(dependency);
            return this;
        }

        public Builder after(long delay) {
            // setup dummy ValueAnimator just to run the clock
            SimpleValueAnimator anim = new SimpleValueAnimator();
            anim.setDuration(delay);
            after(anim);
            return this;
        }

        public void printDependencyHierarchy(){
            Node mCurrentNode = this.mCurrentNode;
            for(Map.Entry<SimpleValueAnimator, Node> entry:mNodeMap.entrySet()){
                SimpleValueAnimator valueAnimator = entry.getKey();
                Node node = entry.getValue();
//                for(Dependency dependency:node.dependencies){
//                    Node dependentNode = dependency.node;
//                    SimpleValueAnimator animation = dependentNode.animation;
//
//                }

            }

        }

    }
}
