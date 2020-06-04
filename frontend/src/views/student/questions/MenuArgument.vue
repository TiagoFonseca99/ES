<template>
    <v-dialog
            :value="dialog"
            @input="$emit('dialog', false)"
            @keydown.esc="$emit('dialog', false)"
            max-width="40%"
            max-height="80%"
    >
        <v-card>
            <v-card-title>
        <span class="headline">{{
          'Do you want to give an argument?'
        }}</span>
            </v-card-title>

            <v-card-actions>
                <v-spacer />
                <v-btn color="primary" data-cy="YesButton" @click="giveArgument"
                >Yes</v-btn
                >
                <v-spacer />
                <v-btn color="primary" data-cy="NoButton" @click="submitQuestion"
                >No</v-btn
                >
                <v-spacer />
            </v-card-actions>
            <give-argument
                    v-if="editSubmission"
                    v-model="GiveArgument"
                    :submission="editSubmission"
                    :old-question-id="oldQuestionId"
                    v-on:argument-given="onSaveChange"
            />
        </v-card>
    </v-dialog>
</template>

<script lang="ts">
    import { Component, Model, Prop, Vue } from 'vue-property-decorator';
    import RemoteServices from '@/services/RemoteServices';
    import Submission from '@/models/management/Submission';
    import GiveArgument from '@/views/student/questions/GiveArgument.vue';

    @Component({
        components: {
            'give-argument': GiveArgument
        }
    })
    export default class MenuArgument extends Vue {
        @Prop({ type: Submission, required: true }) submission!: Submission;
        @Prop({ type: Number, required: false }) oldQuestionId: number | null = null;
        @Model('dialog', Boolean) dialog!: boolean;

        editSubmission!: Submission;
        GiveArgument: boolean = false;

        async created() {
            this.editSubmission = this.submission;
        }

        async submitQuestion() {
            try {
                const result =
                    this.oldQuestionId != null
                        ? await RemoteServices.resubmitQuestion(this.editSubmission, this.oldQuestionId)
                        : await RemoteServices.submitQuestion(this.editSubmission);

                this.$emit('no-changes', result);
            } catch (error) {
                await this.$store.dispatch('error', error);
            }
        }

        giveArgument() {
            this.GiveArgument = true;
        }

        async onSaveChange() {
            this.$emit('no-changes');
            this.GiveArgument = false;
        }
    }
</script>
